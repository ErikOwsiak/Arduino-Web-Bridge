
package tech.infomatrix.arduinowebgate;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.ParcelUuid;

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

    public void StartBlueDev(){
        try {
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            BluetoothDevice bluetoothDevice = this.bluetoothDeviceByMac(mac);
            BluetoothSocket bluetoothSocket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            int bstate = bluetoothDevice.getBondState();
            for (ParcelUuid u : bluetoothDevice.getUuids())
                WebBox.appLog(u.toString());
            OutputStream outputStream = bluetoothSocket.getOutputStream();
            InputStream inputStream = bluetoothSocket.getInputStream();
            this.startReadBlueUart(inputStream);
            //beginListenForData();
            //bluetoothSocket.close();

            WebBox.appLog("StartBlueDev");
            this.callFeedback = new ExeCallFeedback(0, "msg", "rval");

        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebBox.appLog(e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
        }
    }

}
