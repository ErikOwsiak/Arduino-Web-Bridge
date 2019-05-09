
package tech.infomatrix.arduinowebgate;

import android.os.Environment;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import static java.lang.System.out;


public class WebBoxAdminThread implements Runnable {

    static String IDX_FILE = "/idx.html";
    static String HTTP_ERROR_404 = "404 Page Not Found";
    static String HTTP_CODE_200 = "200 OK";

    private Socket reqSocket;

    /* header buffer */
    private PrintWriter hdrBuffer = null;

    /* data buffer to out sock */
    private BufferedOutputStream outBuff = null;

    /* req file name */
    private String fileReq = null;
    private byte[] tmpByteBuff;
    //private WebBoxAdminThread ;


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
            this.cleanUP();
            out.println("finally...");
        }
    }

    private void processRequest() {
        try {

            /* read chars from the client via input stream on the socket */
            BufferedReader inBuff =
                    new BufferedReader(new InputStreamReader(this.reqSocket.getInputStream()));

            // character output stream to client (for headers)
            this.hdrBuffer = new PrintWriter(this.reqSocket.getOutputStream());

            /* get binary output stream to client (for requested data) */
            this.outBuff = new BufferedOutputStream(this.reqSocket.getOutputStream());

            /* get first line of the request from the client */
            String input = inBuff.readLine();

            /* parse request with a string tokenizer */
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase();


            /* get the HTTP method of the client
                we get file requested */
            this.fileReq = parse.nextToken().trim();
            if (this.fileReq.equals("/") || this.fileReq.equals(""))
                this.fileReq = IDX_FILE;

            /* serve */
            if (method.equals("GET")) {
                exeGet();
            } else if (method.equals("POST")) {
                /* - - */
                String exepath = (this.fileReq.startsWith("/")) ?
                        this.fileReq.substring(1) : this.fileReq;
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
            out.println(e);
        }
    }

    /* all files are server from admin folder & sub folder */
    private void exeGet() throws IOException {

        /*WebBox.appLog("\n\n --- exeGet --- ");
        WebBox.appLog("frq: " + this.fileReq);*/

        /* read file */
        int contlen = 0;
        String contype = "text/html";
        String errcode = HTTP_CODE_200;

        /* - - */
        int dotpos = this.fileReq.lastIndexOf(".");
        String fileExt =  this.fileReq.substring(dotpos);
        if (this.fileReq.equals(IDX_FILE)) {
            File f = new File(WebBox.appDir, "admin/idx.html");
            WebBox.appLog("f: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = HTTP_ERROR_404;
            }
            /* js files */
        } else if (fileExt.equals(".js")) {
            contype = "text/javascript";
            File f = new File(WebBox.appDir, "admin/js/" + this.fileReq);
            WebBox.appLog("f: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = HTTP_ERROR_404;
            }
        } else if (fileExt.equals(".css")) {
            contype = "text/css";
            File f = new File(WebBox.appDir, "admin/css/" + this.fileReq);
            out.println("f: " + f.getAbsolutePath());
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = HTTP_ERROR_404;
            }
        } else if (fileExt.equals(".jpg") || fileExt.equals(".png")) {
            contype = (fileExt.equals(".jpg")) ? "image/jpeg" : "image/png";
            File f = new File(WebBox.appDir, "admin/imgs/" + this.fileReq);
            if (f.exists() && f.isFile()) {
                this.tmpByteBuff = this.readFileBytes(f);
                contlen = this.tmpByteBuff.length;
            } else {
                errcode = HTTP_ERROR_404;
            }
        } else {
            contype = "";
            errcode = HTTP_ERROR_404;
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
        byte[] thend = {'\n', '\n'};
        this.outBuff.write(thend);
        this.outBuff.flush();
        this.hdrBuffer.close();
        this.outBuff.close();

        /* - - */
        this.reqSocket.close();

    }

    private void exePost() throws IOException {

        StringBuilder jsonOut = new StringBuilder(8000);
        jsonOut.append("{\"AruWebGate\": {\"p1\": 9, \"p2\": 8}}");
        String xbuff = jsonOut.toString();
        this.tmpByteBuff = xbuff.getBytes();

        /* set headers */
        this.loadBasicHeaders(HTTP_CODE_200, "text/json", xbuff.length());

        /* very important */
        this.hdrBuffer.append('\n');
        this.hdrBuffer.flush();

        /* data */
        if((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        byte[] thend = {'\n', '\n'};
        this.outBuff.write(thend);
        this.outBuff.flush();
        this.hdrBuffer.close();
        this.outBuff.close();

        /* - - */
        this.reqSocket.close();

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
            this.fileReq = "";
        }catch (IOException e) {
            e = null;
        }
    }

    private boolean loadBasicHeaders(String httpcode, String contype, int contlen) {
        this.hdrBuffer.append(String.format("HTTP/1.1 %s\n", httpcode));
        this.hdrBuffer.append("Server: Arudino Web Gate: 0.1\n");
        this.hdrBuffer.append(String.format("Date: %s\n", new Date().toString()));
        this.hdrBuffer.append(String.format("Content-type: %s\n", contype));
        this.hdrBuffer.append(String.format("Content-length: %s\n", contlen));
        return true;
    }

    private boolean exeExe(String[] args) throws IOException {

        StringBuilder jsonOut = new StringBuilder(8000);
        jsonOut.append("{\"AruWebGate\": {\"p1\": 9, \"p2\": 8}}");
        String xbuff = jsonOut.toString();
        this.tmpByteBuff = xbuff.getBytes();

        /* set headers */
        this.loadBasicHeaders(HTTP_CODE_200, "text/json", xbuff.length());

        /* very important */
        this.hdrBuffer.append('\n');
        this.hdrBuffer.flush();

        /* data */
        if ((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        byte[] thend = {'\n', '\n'};
        this.outBuff.write(thend);
        this.outBuff.flush();
        this.hdrBuffer.close();
        this.outBuff.close();

        /* - - */
        this.reqSocket.close();

        return true;
    }

    private boolean exeApi(String[] args) throws IOException {

        ApiCalls apiCalls = new ApiCalls(args);
        apiCalls.execute();
        String apiMsg = apiCalls.apiCallFeedback.toJsonStr();
        this.tmpByteBuff = apiMsg.getBytes();

        /* set headers */
        this.loadBasicHeaders(HTTP_CODE_200, "text/json", apiMsg.length());

        /* very important */
        this.hdrBuffer.append('\n');
        this.hdrBuffer.flush();

        /* data */
        if ((this.tmpByteBuff != null) && (this.tmpByteBuff.length > 0))
            this.outBuff.write(this.tmpByteBuff);

        /* the end */
        byte[] thend = {'\n', '\n'};
        this.outBuff.write(thend);
        this.outBuff.flush();
        this.hdrBuffer.close();
        this.outBuff.close();

        /* - - */
        this.reqSocket.close();

        return true;

    }
}
