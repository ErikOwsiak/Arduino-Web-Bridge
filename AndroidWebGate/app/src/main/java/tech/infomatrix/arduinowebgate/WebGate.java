
package tech.infomatrix.arduinowebgate;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.WorkSource;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.lang.System.out;


public class WebGate {

    final public static int adminPort = 8020;
    final public static String devMaker = Build.MANUFACTURER;
    final public static String devModel = android.os.Build.MODEL;
    final public static File extDir = Environment.getExternalStorageDirectory();
    final public static String appDirName = "ArduWebBox";
    final public static String appConfName = "app.conf";
    final public static String isFakeSD = (Environment.isExternalStorageEmulated()) ? "Yes" : "No";

    public static Context ctx = null;
    public static File appDir = null;
    public static String ipAddress = null;
    public static Thread adminThread = null;
    public TextView tvFeedback = null;

    private ServerSocket adminSocket;


    public WebGate() {
    }

    public void startClientServer() {
    }

    public void startAdminServer() {
        /* - - */
        WebGate.adminThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /* - - */
                    ServerSocket adminSocket = new ServerSocket(WebGate.adminPort);
                    WifiManager wm = (WifiManager) WebGate.ctx.getSystemService(Context.WIFI_SERVICE);
                    String msg = "Admin server started.\n" +
                            "On IP: " + adminSocket.getInetAddress().getHostAddress() + "\n" +
                            "On port : " + WebGate.adminPort + "\n" +
                            "MAC: " + wm.getConnectionInfo().getMacAddress() + "\n";
                    /* - - */
                    while (true) {
                        WebBoxAdminThread adminRequestThread =
                                new WebBoxAdminThread(adminSocket.accept());
                        Thread t = new Thread(adminRequestThread);
                        t.start();
                    }
                    /* - - */
                } catch (IOException e) {
                    out.println("q0: " + e.toString());
                } catch (Exception e) {
                    out.println("q1: " + e.toString());
                }
            }
        });
        /* - - */
        WebGate.adminThread.start();
    }

    public Boolean isSetup() {
        String fn = String.format("%s/%s/%s", WebGate.extDir, WebGate.appDirName, WebGate.appConfName);
        return new File(fn).isFile();
    }

    public String readAppConf() throws IOException {
        /* - - */
        out.println(" --- reading conf file --- ");
        File conf = new File(this.appDir, "app.conf");
        BufferedReader br = new BufferedReader(new FileReader(conf));
        /* - - */
        String str;
        while ((str = br.readLine()) != null)
            out.println(str);
        return null;
    }

    public void runSetup() throws IOException {
        this.createAppFolders();
        this.writeConfFile();
    }

    private Boolean createAppFolders() {
        /* - - */
        WebGate.appDir = new File(WebGate.extDir, WebGate.appDirName);
        if (!WebGate.appDir.exists()) {
            out.println("creating app folders");
            if (WebGate.appDir.mkdir()) {
                out.println(WebGate.appDir + " : Created");
                /* app dir */
                String[] sdirs = {"admin", "js", "css", "exe", "data"};
                for (String s : sdirs) {
                    File f = new File(WebGate.appDir, s);
                    if (f.mkdir())
                        f.setWritable(true);
                }
                /* admin dir */
                File admin = new File(WebGate.appDir, "admin");
                String[] adirs = {"js", "css", "exe"};
                for (String s : adirs) {
                    File f = new File(admin, s);
                    if (f.mkdir())
                        f.setWritable(true);
                }
            } else {
                out.println(WebGate.appDir.getPath() + " : NotCreated");
            }
        } else {
            out.println(WebGate.appDir.getPath() + " : Exists");
        }
        /* - - */
        return WebGate.appDir.exists();
    }

    private void writeConfFile() throws IOException {
        try {
            File conf = new File(WebGate.appDir, "admin/app.conf");
            FileWriter fw = new FileWriter(conf, true);
            fw.append("IsFakeSD: " + WebGate.isFakeSD + "\n");
            fw.append("DateCreated: " + new Date().toString());
            fw.close();
            if (conf.exists())
                out.println("app.conf found");
            else
                out.println("app conf not found");
        } catch (IOException e) {
            out.println(e.toString());
        } catch (Exception e) {
            out.println(e.toString());
        }
    }

    public static void appLog(String msg) {
        out.println(msg);
    }

    public Boolean setLocalAddress() throws SocketException {

        Boolean rval = false;
        List<NetworkInterface> interfaces =
                Collections.list(NetworkInterface.getNetworkInterfaces());
        try {

            for (NetworkInterface ni : interfaces) {
                if (!ni.getName().equals("wlan0"))
                    continue;
                List<InetAddress> iads = Collections.list(ni.getInetAddresses());
                for (InetAddress iad : iads) {
                    if (iad.isLoopbackAddress())
                        continue;
                    if (iad.isSiteLocalAddress()) {
                        WebGate.ipAddress = iad.getHostAddress();
                        rval = true;
                    }
                }
            }

        } catch (Exception e) {
            WebGate.appLog(e.toString());
        }

        return rval;

    }
}
