
package tech.infomatrix.arduinowebgate;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import static java.lang.System.out;


public class RequestParser {

    static String IDX_FILE = "/idx.html";
    static String HTTP_ERROR_404 = "404 Page Not Found";
    static String HTTP_ERROR_406 = "406 Not Acceptable";
    static String HTTP_ERROR_500 = "500 Internal Server Error";
    static String HTTP_CODE_200 = "200 OK";
    static String CT_HTML = "text/html";
    static String CT_JSON = "text/json";
    static int CONTENT_LENGTH = 0;

    public List<String> requestHeaders;
    public char[] requestBody;
    public Hashtable<String, String> postDict;
    public String method;
    public String requestFile;
    public int errorCount = 0;

    private BufferedReader bufferedReader;


    public RequestParser(BufferedReader bufferedReader) {
        this.bufferedReader = bufferedReader;
        this.requestHeaders = new LinkedList<String>();
    }

    public void basicParse() throws IOException {
        try {

            /* load headers */
            String line = this.bufferedReader.readLine();
            while (line.length() > 0) {
                this.requestHeaders.add(line);
                WebBox.appLog(line);
                line = this.bufferedReader.readLine();
                if (line.toLowerCase().startsWith("content-length")) {
                    String strlen = line.split(":")[1].trim();
                    RequestParser.CONTENT_LENGTH = Integer.parseInt(strlen);
                }
            }

            /* load body char by char */
            int idx = 0;
            char ch = (char) 0;
            if (RequestParser.CONTENT_LENGTH > 0) {
                this.requestBody = new char[RequestParser.CONTENT_LENGTH];
                do {
                    if (!this.bufferedReader.ready())
                        break;
                    ch = (char) this.bufferedReader.read();
                    this.requestBody[idx++] = ch;
                } while (ch != (char) 0);
                /* - - */
                this.createPostDict();
            }

            /* method */
            StringTokenizer parse = new StringTokenizer(this.requestHeaders.get(0));
            this.method = parse.nextToken().toUpperCase();

            /* file requested */
            String token = parse.nextToken().trim();
            if (token.equals("/") || token.equals(""))
                token = RequestParser.IDX_FILE;

            /* set request file/exe path */
            this.requestFile = token;


        } catch (Exception e) {
            WebBox.appLog(e.toString());
            this.errorCount++;
        }
    }

    private void createPostDict() {
        try {
            String[] kv = null;
            this.postDict = new Hashtable<String, String>();
            String str = URLDecoder.decode(new String(this.requestBody));
            for (String s : str.split("&")) {
                kv = s.split("=");
                this.postDict.put(kv[0], kv[1]);
            }
        } catch (Exception e) {
            WebBox.appLog(e.toString());
            this.postDict = null;
            this.errorCount++;
        }
    }

}
