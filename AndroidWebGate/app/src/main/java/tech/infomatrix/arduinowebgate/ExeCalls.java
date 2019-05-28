
package tech.infomatrix.arduinowebgate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;
import android.util.Pair;

import java.io.IOException;
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
            WebGate.appLog(e.toString());
            return false;
        }
        /* - - */
        return true;
    }

    public void ConnectBlueDev() {
        try {
            String mac = this.postDict.get("ADR").trim();
            /* todo: msg that it is running */
            if(UartGate.uartThreads.containsKey(mac)) {
                this.callFeedback = new ExeCallFeedback(0, "OK", "Running");
                return;
            }
            /* - - */
            Pair<OutputStream, InputStream> streams =
                    UartGate.uartGate.bluetoothStreamsFromMac(mac);
            UartGate.uartGate.startBluetoothReader(mac, streams.second);
            this.callFeedback = new ExeCallFeedback(0, "OK", "Connected");
        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebGate.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        } catch (Exception e) {
            WebGate.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        }
    }

    public void DisconnectBlueDev() throws IOException {
        String mac = this.postDict.get("ADR").trim();
        try {
            /* todo: msg that it is running */
            if (UartGate.uartThreads.containsKey(mac)) {
                Thread t = UartGate.uartThreads.get(mac);
                assert t != null;
                t.interrupt();
                UartGate.uartThreads.remove(mac);
                /* - - */
                BluetoothSocket bluetoothSocket = UartGate.bluetoothSockets.get(mac);
                assert bluetoothSocket != null;
                bluetoothSocket.close();
                /* clear refs */
                UartGate.bluetoothSockets.remove(mac);
                UartGate.uartInBuffers.remove(mac);
                this.callFeedback = new ExeCallFeedback(0, "OK", "ReaderStopped");
            } else {
                this.callFeedback = new ExeCallFeedback(0, "OK", "ReaderNotFound");
            }
        } catch (IOException e) {
            WebGate.appLog(e.toString());
        } catch (Exception e) {
            WebGate.appLog(e.toString());
        }
    }

    public void CheckBlueDev() throws IOException {
        String mac = this.postDict.get("ADR").trim();
        try {
            /* todo: msg that it is running */
            if (UartGate.uartThreads.containsKey(mac)) {
                Thread t = UartGate.uartThreads.get(mac);
                assert t != null;
                String isalive = (t.isAlive()) ? "Alive" : "Dead";
                /* - - */
                BluetoothSocket bluetoothSocket = UartGate.bluetoothSockets.get(mac);
                assert bluetoothSocket != null;
                String isconn = (bluetoothSocket.isConnected()) ? "Connected" : "Disconnected";
                /* clear refs */
                UartGate.bluetoothSockets.remove(mac);
                UartGate.uartInBuffers.remove(mac);
                String rval = String.format("ReaderIs%s%s", isalive, isconn);
                this.callFeedback = new ExeCallFeedback(0, "OK", "ReaderIs");
            } else {
                this.callFeedback = new ExeCallFeedback(0, "OK", "ReaderNotFound");
            }
        } catch (Exception e) {
            WebGate.appLog(e.toString());
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
            WebGate.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        } catch (Exception e) {
            WebGate.appLog(e.toString());
            this.callFeedback = new ExeCallFeedback(1, "ERROR", e.toString());
        }
    }

}
