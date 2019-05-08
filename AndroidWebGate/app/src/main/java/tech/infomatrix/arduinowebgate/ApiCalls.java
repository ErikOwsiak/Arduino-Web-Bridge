
package tech.infomatrix.arduinowebgate;

import android.app.admin.DeviceAdminService;
import android.hardware.usb.UsbManager;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.android.material.internal.*;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;


public class ApiCalls {

    private String[] args;
    public ApiCallFeedback apiCallFeedback;
    public Exception apiExp;
    public static String EXP_MSG = "WrongNumberOfArgs";


    public ApiCalls(String[] args) {
        WebBox.appLog(args.toString());
        this.args = args;
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
            if(this.args.length != 4)
                throw new Exception(ApiCalls.EXP_MSG);
            String tel = this.args[2];
            String msg = URLDecoder.decode(this.args[3]);
            WebBox.appLog(msg);
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
        UartMsg msg = UartGate.uartBuffer.read();
        String strmsg = (msg == null) ? "NoData" : msg.toString();
        this.apiCallFeedback = new ApiCallFeedback(0, "OK", strmsg);
    }

}
