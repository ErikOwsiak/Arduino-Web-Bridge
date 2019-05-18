package tech.infomatrix.arduinowebgate;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;


public class MsgProccessor {

    public void callUrl(final String[] args) {
        /* spin task */
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {

                    /* http://ardugate.infomatrix.tech/test/1 */
                    char ch;
                    int idx = 0;
                    char[] chbuff = new char[128];
                    URL url = new URL("http://ardugate.infomatrix.tech/test/1");
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

                    /* thread loop */
                    while (true) {
                        /* check for char */
                        if (inputStream.available() == 0)
                            continue;
                        /* read stream */
                        ch = (char) inputStream.read();
                        if (ch != '\n') {
                            chbuff[idx++] = ch;
                        } else {
                            String msg = new String(chbuff, 0, idx);
                            Arrays.fill(chbuff, (char) 0);
                            idx = 0;
                        }
                    }

                } catch (Exception e) {
                    WebBox.appLog(e.toString());
                } finally {
                    urlConnection.disconnect();
                }
            }
        });
        /* - - */
        t.start();
    }
}
