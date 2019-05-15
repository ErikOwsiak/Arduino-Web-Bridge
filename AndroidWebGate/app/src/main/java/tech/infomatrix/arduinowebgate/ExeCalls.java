
package tech.infomatrix.arduinowebgate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Pair;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.UUID;


public class ExeCalls {

    public ExeCallFeedback callFeedback;
    public Exception apiExp;
    public static String EXP_MSG = "WrongNumberOfArgs";
    private String[] args = null;
    private Hashtable<String, String> postDict = null;


    public ExeCalls(String[] args, Hashtable<String, String> postDict) {
        this.args = args;
        this.postDict = postDict;
    }

    public boolean execute() {
        try {
            Method method = this.getClass().getMethod(this.args[1]);
            method.invoke(this);
        } catch (Exception e) {
            WebBox.appLog(e.toString());
            return false;
        }
        /* - - */
        return true;
    }

    public void StartBlueDev() {
        try {
            String mac = this.postDict.get("ADR").trim();
            /* todo: msg that it is running */
            if(UartGate.uartThreads.containsKey(mac)) {
                this.callFeedback = new ExeCallFeedback(0, "OK", "Reader is running!");
                return;
            }
            /* - - */
            Pair<OutputStream, InputStream> streams =
                    UartGate.uartGate.bluetoothStreamsFromMac(mac);
            UartGate.uartGate.startBluetoothReader(mac, streams.second);
            this.callFeedback = new ExeCallFeedback(0, "OK", "Started");
        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebBox.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        }
    }

    public void ReadBlueDevBuffer() {
        try {
            String mac = this.postDict.get("ADR").trim();
            UartGateBuffer uartGateBuffer = UartGate.uartInBuffers.get(mac);
            UartMsg rval = uartGateBuffer.read();
            this.callFeedback = new ExeCallFeedback(0, "OK", rval.toString());
        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebBox.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        }
    }

}
