
package tech.infomatrix.arduinowebgate;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import static java.lang.String.*;
import static java.lang.System.out;


public class WebBoxAdminThread implements Runnable {


    private Socket reqSocket;

    /* header buffer to out sock */
    private PrintWriter hdrBuffer = null;

    /* data buffer to out sock */
    private BufferedOutputStream dataBuff = null;

    /* - - */
    private byte[] tmpByteBuff;
    private RequestParser requestParser;


    public WebBoxAdminThread(Socket soc) {
        this.reqSocket = soc;
    }


    @Override
    public void run() {
        try {
            this.processRequest();
        } catch (Exception e) {
            out.println("x1: " + e.toString());
        } finally {
            out.println("run() -> finally...");
        }
    }

    private void processRequest() throws IOException {
        try {

            /* read chars from the client via input stream on the socket */
            BufferedReader inBuff =
                    new BufferedReader(new InputStreamReader(this.reqSocket.getInputStream()));

            /* character output stream to client (for headers) */
            this.hdrBuffer = new PrintWriter(this.reqSocket.getOutputStream());

            /* get binary output stream to client (for requested data) */
            this.dataBuff = new BufferedOutputStream(this.reqSocket.getOutputStream());

            this.requestParser = new RequestParser(inBuff);
            this.requestParser.basicParse();
            if (this.requestParser.errorCount > 0)
                WebBox.appLog("ParserErrors :(");

            /* serve */
            if (this.requestParser.method.equals("GET")) {
                this.exeGet();
            } else if (this.requestParser.method.equals("POST")) {
                /* - - */
                String exepath = (this.requestParser.requestFile.startsWith("/")) ?
                        this.requestParser.requestFile.substring(1) :
                        this.requestParser.requestFile;
                /* - - */
                String[] args = exepath.split("/");
                if (args[0].equals("exeapi"))
                    this.exeApi(args);
                else if (args[0].equals("exe"))
                    this.exeExe(args);
                else
                    this.exePost();
                /* - - */
            }

        } catch (Exception e) {
            out.println(e.toString());
            /* todo: push to 500 page */
            this.send2ErrorPage();

        }
    }

    /* all files are server from admin folder & sub folder */
    private void exeGet() throws IOException {

        /* set defaults */
        int contlen = 0;
        String contype = "";
        String errcode = RequestParser.HTTP_CODE_200;

        /* - - */
        File reqfile = null;
        int dotpos = this.requestParser.requestFile.lastIndexOf(".");
        String fileExt = this.requestParser.requestFile.substring(dotpos);
        if (this.requestParser.requestFile.equals(RequestParser.IDX_FILE)) {
            reqfile = new File(WebBox.appDir, "admin/idx.html");
        } else if (fileExt.equals(".js")) {
            reqfile = new File(WebBox.appDir, "admin/js/" + this.requestParser.requestFile);
        } else if (fileExt.equals(".css")) {
            reqfile = new File(WebBox.appDir, "admin/css/" + this.requestParser.requestFile);
        } else if (".jpg.png.svg".contains(fileExt)) {
            reqfile = new File(WebBox.appDir, "admin/imgs/" + this.requestParser.requestFile);
        } else {
            errcode = RequestParser.HTTP_ERROR_406;
        }

        try {
            /* read file */
            if ((reqfile != null) && (reqfile.exists()) && (reqfile.isFile())) {
                this.tmpByteBuff = this.readFileBytes(reqfile);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = RequestParser.HTTP_ERROR_404;
            }
        } catch (Exception e) {
            /* todo: log e */
            errcode = RequestParser.HTTP_ERROR_500;
        }

        /* set headers */
        contype = MimeTypes.fromExt(fileExt);
        this.loadResponseHeaders(errcode, contype, contlen);
        this.loadResponseBody();
        /* the end */
        this.theEnd();

    }

    private void exePost() throws IOException {
        /* todo: implement */
        StringBuilder jsonOut = new StringBuilder(8000);
        jsonOut.append("{\"AruWebGate\": {\"p1\": 9, \"p2\": 8}}");
        String xbuff = jsonOut.toString();
        this.tmpByteBuff = xbuff.getBytes();
        /* set headers */
        this.loadResponseHeaders(RequestParser.HTTP_CODE_200, MimeTypes.CT_JSON, xbuff.length());
        this.loadResponseBody();
        /* the end */
        this.theEnd();
    }

    private void exeExe(String[] args) throws IOException {
        try {
            ExeCalls exeCalls = new ExeCalls(args, this.requestParser.postDict);
            exeCalls.execute();
            assert exeCalls.callFeedback != null;
            String callMsg = exeCalls.callFeedback.toJsonStr();
            this.tmpByteBuff = callMsg.getBytes();
            /* set headers */
            this.loadResponseHeaders(RequestParser.HTTP_CODE_200, MimeTypes.CT_JSON, callMsg.length());
            this.loadResponseBody();
            /* the end */
            this.theEnd();
        } catch (NullPointerException e) {
            WebBox.appLog(e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
        }
    }

    private void exeApi(String[] args) throws IOException {
        /* api wrapper */
        ApiCalls apiCalls = new ApiCalls(args, this.requestParser.postDict);
        apiCalls.execute();
        String apiMsg = apiCalls.apiCallFeedback.toJsonStr();
        /* pack api response */
        this.tmpByteBuff = apiMsg.getBytes();
        /* set headers */
        this.loadResponseHeaders(RequestParser.HTTP_CODE_200, RequestParser.CT_JSON, apiMsg.length());
        this.loadResponseBody();
        /* the end */
        this.theEnd();
    }

    private void loadResponseHeaders(String httpcode, String contype, int contlen) {
        /* load all headers here */
        this.hdrBuffer.append(format("HTTP/1.1 %s\r\n", httpcode));
        this.hdrBuffer.append("Server: Arudino Web Gate v0.1\r\n");
        this.hdrBuffer.append(format("Date: %s\r\n", new Date().toString()));
        this.hdrBuffer.append(format("Content-type: %s\r\n", contype));
        this.hdrBuffer.append(format("Content-length: %s\r\n", contlen));
        this.hdrBuffer.append(format("Device-Maker: %s\r\n", WebBox.devMaker));
        this.hdrBuffer.append(format("Device-Model: %s\r\n", WebBox.devModel));
        this.hdrBuffer.append(format("Set-Cookie: %s\r\n",
                format("devMaker=%s;", WebBox.devMaker)));
        this.hdrBuffer.append(format("Set-Cookie: %s\r\n",
                format(" devModel=%s;", WebBox.devModel)));
        /* headers - body spacer */
        this.hdrBuffer.append("\r\n");
        this.hdrBuffer.flush();
    }

    private void send2ErrorPage() throws IOException {
        /* load all headers here */
        this.hdrBuffer.append(format("HTTP/1.1 %s\r\n", RequestParser.HTTP_ERROR_500));
        this.hdrBuffer.append("Server: Arudino Web Gate v0.1\r\n");
        this.hdrBuffer.append(format("Date: %s\r\n", new Date().toString()));
        this.hdrBuffer.append(format("Device-Maker: %s\r\n", WebBox.devMaker));
        this.hdrBuffer.append(format("Device-Model: %s\r\n", WebBox.devModel));
        this.hdrBuffer.append(format("Set-Cookie: %s\r\n",
                format("devMaker=%s;", WebBox.devMaker)));
        this.hdrBuffer.append(format("Set-Cookie: %s\r\n",
                format(" devModel=%s;", WebBox.devModel)));
        /* headers - body spacer */
        this.hdrBuffer.append("\r\n");
        this.hdrBuffer.flush();
        /* - - */
        this.theEnd();
    }

    private void loadResponseBody() throws  IOException {
        /* data */
        if ((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.dataBuff.write(this.tmpByteBuff);
        /* mark end */
        this.dataBuff.write(new byte[]{'\r', '\n', '\r', '\n'});
        this.dataBuff.flush();
    }

    private byte[] readFileBytes(File file) throws IOException {
        byte[] bytes = null;
        FileInputStream fis = null;
        try {
            int byteCount = (int) file.length();
            bytes = new byte[byteCount];
            fis = new FileInputStream(file);
            fis.read(bytes);
        } catch (IOException e) {
            out.println(e.toString());
        } finally {
            if (fis != null)
                fis.close();
        }
        /* - - */
        return bytes;
    }

    private void theEnd() throws IOException {
        try {
            /* close */
            this.hdrBuffer.close();
            this.dataBuff.close();
            /* sock out ;) */
            if (!this.reqSocket.isClosed())
                this.reqSocket.close();
            /* null out */
            this.reqSocket = null;
            this.hdrBuffer = null;
            this.tmpByteBuff = null;
            this.requestParser = null;
            /* - - */
        } catch (Exception e) {
            /* todo: trace it */
            WebBox.appLog(e.toString());
        } finally {
            /* give it last try */
            if ((this.reqSocket != null) && (!this.reqSocket.isClosed()))
                this.reqSocket.close();
        }
    }

}
