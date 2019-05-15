
package tech.infomatrix.arduinowebgate;

import android.app.admin.DeviceAdminService;
import android.hardware.usb.UsbManager;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.android.material.internal.*;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import static android.content.ContentValues.TAG;


public class ApiCalls {

    public ApiCallFeedback apiCallFeedback;
    public Exception apiExp;
    public static String EXP_MSG = "WrongNumberOfArgs";

    private String[] args = null;
    private Hashtable<String, String> postDict = null;


    public ApiCalls(String[] args, Hashtable<String, String> postDict) {
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

    public void SendSms() {
        try {
            /* check num of args */
            if((this.postDict == null) || (this.postDict.size() != 2))
                throw new Exception(ApiCalls.EXP_MSG);
            /* TNUM, SMSTXT */
            String tel = this.postDict.get("TNUM");
            String msg = this.postDict.get("SMSTXT");
            /* - - */
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(tel, null, msg, null, null);
            this.apiCallFeedback = new ApiCallFeedback(0, "OK", "Sms Sent");
        } catch (Exception e) {
            this.apiExp = e;
            WebBox.appLog(e.toString());
        }
    }

    public void ScanUarts() {
        String buff = UartGate.uartGate.listBluetoothDevices();
        this.apiCallFeedback = new ApiCallFeedback(0, "OK", buff);
    }

    public void ScanBluetooth() {
        UartGate.uartGate.bluetoothDeviceInfo("98:D3:31:50:3D:3A");
        String buff = UartGate.uartGate.listBluetoothDevices();
        this.apiCallFeedback = new ApiCallFeedback(0, "OK", buff);
    }

    public void PhoneDateTime() {
        Date d = new Date();
        String rval = String.format("%s %s", d.getTime(), d.toString());
        this.apiCallFeedback = new ApiCallFeedback(0, "OK", rval);
    }

    public void PeekUartBuffer(){
        /*UartMsg msg = UartGate.uartBuffer.read();
        String strmsg = (msg == null) ? "NoData" : msg.toString();
        this.apiCallFeedback = new ApiCallFeedback(0, "OK", strmsg);*/
    }

}
