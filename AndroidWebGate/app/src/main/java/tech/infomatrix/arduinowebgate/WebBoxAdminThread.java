
package tech.infomatrix.arduinowebgate;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import static java.lang.System.out;


public class WebBoxAdminThread implements Runnable {

    private Socket reqSocket;

    /* header buffer */
    private PrintWriter hdrBuffer = null;

    /* data buffer to out sock */
    private BufferedOutputStream outBuff = null;

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

    private void processRequest() {
        try {

            /* read chars from the client via input stream on the socket */
            BufferedReader inBuff =
                    new BufferedReader(new InputStreamReader(this.reqSocket.getInputStream()));

            /* character output stream to client (for headers) */
            this.hdrBuffer = new PrintWriter(this.reqSocket.getOutputStream());

            /* get binary output stream to client (for requested data) */
            this.outBuff = new BufferedOutputStream(this.reqSocket.getOutputStream());

            this.requestParser = new RequestParser(inBuff);
            this.requestParser.basicParse();

            /* serve */
            if (this.requestParser.method.equals("GET")) {
                exeGet();
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
        }
    }

    /* all files are server from admin folder & sub folder */
    private void exeGet() throws IOException {

        /* set defaults */
        int contlen = 0;
        String contype = RequestParser.CT_HTML;
        String errcode = RequestParser.HTTP_CODE_200;

        /* - - */
        int dotpos = this.requestParser.requestFile.lastIndexOf(".");
        String fileExt = this.requestParser.requestFile.substring(dotpos);
        if (this.requestParser.requestFile.equals(RequestParser.IDX_FILE)) {
            File f = new File(WebBox.appDir, "admin/idx.html");
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = RequestParser.HTTP_ERROR_404;
            }
            /* js files */
        } else if (fileExt.equals(".js")) {
            contype = RequestParser.CT_JS;
            File f = new File(WebBox.appDir, "admin/js/" + this.requestParser.requestFile);
            WebBox.appLog("f: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = RequestParser.HTTP_ERROR_404;
            }
        } else if (fileExt.equals(".css")) {
            contype = RequestParser.CT_CSS;
            File f = new File(WebBox.appDir, "admin/css/" + this.requestParser.requestFile);
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = RequestParser.HTTP_ERROR_404;
            }
        } else if (fileExt.equals(".jpg") || fileExt.equals(".png")) {
            contype = (fileExt.equals(".jpg")) ? RequestParser.CT_JPG : RequestParser.CT_PNG;
            File f = new File(WebBox.appDir, "admin/imgs/" + this.requestParser.requestFile);
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = RequestParser.HTTP_ERROR_404;
            }
        } else {
            errcode = RequestParser.HTTP_ERROR_404;
            contype = "";
        }

        /* set headers */
        this.loadBasicHeaders(errcode, contype, contlen);
        /* very important */
        this.hdrBuffer.append("\n");

        /* headers */
        this.hdrBuffer.flush();

        /* data */
        if ((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        this.theEnd();

    }

    private void exePost() throws IOException {

        StringBuilder jsonOut = new StringBuilder(8000);
        jsonOut.append("{\"AruWebGate\": {\"p1\": 9, \"p2\": 8}}");
        String xbuff = jsonOut.toString();
        this.tmpByteBuff = xbuff.getBytes();

        /* set headers */
        this.loadBasicHeaders(RequestParser.HTTP_CODE_200, RequestParser.CT_JSON, xbuff.length());

        /* very important */
        this.hdrBuffer.append('\n');
        this.hdrBuffer.flush();

        /* data */
        if((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        this.theEnd();

    }

    private void exeExe(String[] args) throws IOException {

        StringBuilder jsonOut = new StringBuilder(8000);
        jsonOut.append("{\"AruWebGate\": {\"p1\": 9, \"p2\": 8}}");
        String xbuff = jsonOut.toString();
        this.tmpByteBuff = xbuff.getBytes();

        /* set headers */
        this.loadBasicHeaders(RequestParser.HTTP_CODE_200, RequestParser.CT_JSON, xbuff.length());

        /* very important */
        this.hdrBuffer.append('\n');
        this.hdrBuffer.flush();

        /* data */
        if ((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        this.theEnd();

    }

    private void exeApi(String[] args) throws IOException {

        ApiCalls apiCalls = new ApiCalls(args, this.requestParser.postDict);
        apiCalls.execute();
        String apiMsg = apiCalls.apiCallFeedback.toJsonStr();
        this.tmpByteBuff = apiMsg.getBytes();

        /* set headers */
        this.loadBasicHeaders(RequestParser.HTTP_CODE_200, RequestParser.CT_JSON, apiMsg.length());

        /* very important */
        this.hdrBuffer.append('\n');
        this.hdrBuffer.flush();

        /* data */
        if ((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        this.theEnd();

    }

    private boolean loadBasicHeaders(String httpcode, String contype, int contlen) {
        this.hdrBuffer.append(String.format("HTTP/1.1 %s\n", httpcode));
        this.hdrBuffer.append("Server: Arudino Web Gate: 0.1\n");
        this.hdrBuffer.append(String.format("Date: %s\n", new Date().toString()));
        this.hdrBuffer.append(String.format("Content-type: %s\n", contype));
        this.hdrBuffer.append(String.format("Content-length: %s\n", contlen));
        return true;
    }

    private byte[] readFileBytes(File file) throws IOException {

        FileInputStream fis = null;
        int byteCount = (int) file.length();
        byte[] bytes = new byte[byteCount];

        try {
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

    private void cleanUP() {
        try {
            if (this.reqSocket.isClosed())
                this.reqSocket.close();
            this.reqSocket = null;
            this.hdrBuffer = null;
            this.tmpByteBuff = null;
            this.requestParser = null;
        }catch (IOException e) {
            e = null;
        }
    }

    private void theEnd() throws IOException {
        try {
            byte[] thend = {'\n', '\n'};
            this.outBuff.write(thend);
            this.outBuff.flush();
            this.hdrBuffer.close();
            this.outBuff.close();
            this.reqSocket.close();
            /* - - */
            this.cleanUP();
        }catch (Exception e){
            WebBox.appLog(e.toString());
        }
    }

}
