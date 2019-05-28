
package tech.infomatrix.arduinowebgate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.ParcelUuid;
import android.renderscript.Script;
import android.util.Pair;
import android.database.sqlite.*;
import android.widget.MultiAutoCompleteTextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

import static java.lang.String.format;
import static java.lang.System.in;
import static java.lang.System.out;
import static java.lang.System.runFinalization;


public class UartGate {

    public static Context ctx;
    public static UartGate uartGate;
    public static Hashtable<String, UartGateBuffer> uartInBuffers;
    public static Hashtable<String, Thread> uartThreads;
    public static Hashtable<String, BluetoothSocket> bluetoothSockets;
    private static String sppUUID = "00001101-0000-1000-8000-00805f9b34fb";


    public UartGate() {
        UartGate.uartGate = this;
        UartGate.uartInBuffers = new Hashtable<String, UartGateBuffer>();
        UartGate.uartThreads = new Hashtable<String, Thread>();
        UartGate.bluetoothSockets = new Hashtable<String, BluetoothSocket>();
    }

    public String wget() {
        return "";
    }

    public String listUsbDevices() {
        UsbManager manager = (UsbManager) UartGate.ctx.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        if (deviceList.isEmpty())
            return "NoUsbDevicesFound";
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            out.println(device);
        }
        return null;
    }

    public String listBluetoothDevices() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
            return "NoBluetoothFound";
        if (!bluetoothAdapter.isEnabled())
            return "BluetoothNotEnabled";

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        StringBuilder sb = new StringBuilder();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice d : pairedDevices)
                sb.append(format("%s; %s|", d.getName(), d.getAddress()));
        }

        return sb.toString();
    }

    public void bluetoothDeviceInfo(String mac) {
        try {

        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebGate.appLog(e.toString());
        } catch (Exception e) {
            WebGate.appLog(e.toString());
        }
    }

    public Pair<OutputStream, InputStream> bluetoothStreamsFromMac(String mac) {

        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {

            UUID uuid = UUID.fromString(UartGate.sppUUID);
            if (!BluetoothAdapter.checkBluetoothAddress(mac))
                WebGate.appLog("BadMacAddress");

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
            assert bluetoothDevice != null;
            BluetoothSocket bluetoothSocket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            assert bluetoothSocket != null;
            bluetoothSocket.connect();
            UartGate.bluetoothSockets.put(mac, bluetoothSocket);

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            assert ((outputStream != null) && (inputStream != null));

        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebGate.appLog(e.toString());
        } catch (IOException e) {
            /* todo: fix it; connect error; */
            WebGate.appLog(e.toString());
        } catch (Exception e) {
            WebGate.appLog(e.toString());
        }

        /* - - */
        return new Pair<OutputStream, InputStream>(outputStream, inputStream);

    }

    public void startBluetoothReader(final String key, final InputStream inputStream) {

        /* start thread */
        Thread readThread = new Thread(new Runnable() {

            @Override
            public void run() {

                UartGateBuffer uartGateBuffer = null;

                try {

                    /* add read buffer */
                    uartGateBuffer = new UartGateBuffer(key, 0);
                    UartGate.uartInBuffers.put(key, uartGateBuffer);

                    int idx = 0;
                    char ch = (char) 0;
                    char[] chbuff = new char[256];

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
                            String msg = new String(chbuff, 0, --idx);
                            uartGateBuffer.addUartMsg(msg);
                            this.processMsg(msg);
                            Arrays.fill(chbuff, (char) 0);
                            idx = 0;
                        }
                        /* check thread */
                        if (Thread.currentThread().isInterrupted()) {
                            inputStream.close();
                            break;
                        }
                    }

                } catch (IOException e) {
                    WebGate.appLog(e.toString());
                } catch (ThreadDeath e) {
                    WebGate.appLog(e.toString());
                } catch (Exception e) {
                    WebGate.appLog(e.toString());
                } finally {
                    /* make sure */
                    if (uartGateBuffer != null)
                        uartGateBuffer.close();
                    try {
                        if (inputStream != null)
                            inputStream.close();
                    } catch (IOException e) {
                        WebGate.appLog(e.toString());
                    }
                }
            }

            /* process msg; check for actions */
            private void processMsg(String msg) {
                /*StringTokenizer stringTokenizer = new StringTokenizer(msg);*/
                MsgProccessor msgProccessor = new MsgProccessor();
                String[] msgbuff = msg.split(";");
                if (msgbuff[0].startsWith("URL"))
                    msgProccessor.callUrl(msgbuff);
            }

        });

        /* - - */
        UartGate.uartThreads.put(key, readThread);
        readThread.start();

    }

}
