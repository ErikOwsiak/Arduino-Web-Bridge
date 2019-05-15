
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
import android.util.Pair;
import android.database.sqlite.*;

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
    private static String sppUUID = "00001101-0000-1000-8000-00805f9b34fb";


    public UartGate() {
        UartGate.uartGate = this;
        UartGate.uartInBuffers = new Hashtable<String, UartGateBuffer>();
        UartGate.uartThreads = new Hashtable<String, Thread>();
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

            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            /*BluetoothDevice bluetoothDevice = this.bluetoothDeviceByMac(mac);
            BluetoothSocket bluetoothSocket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket.connect();
            int bstate = bluetoothDevice.getBondState();

            for (ParcelUuid u : bluetoothDevice.getUuids())
                WebBox.appLog(u.toString());

            OutputStream outputStream = bluetoothSocket.getOutputStream();
            InputStream inputStream = bluetoothSocket.getInputStream();*/

            /* - - */
            //this.startBlueDevice(inputStream);
            //beginListenForData();
            //bluetoothSocket.close();

        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebBox.appLog(e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
        }
    }

    public Pair<OutputStream, InputStream> bluetoothStreamsFromMac(String mac) {

        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {

            UUID uuid = UUID.fromString(UartGate.sppUUID);
            if (!BluetoothAdapter.checkBluetoothAddress(mac))
                WebBox.appLog("BadMacAddress");

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
            BluetoothSocket bluetoothSocket =
                    bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebBox.appLog(e.toString());
        } catch (IOException e) {
            /* todo: fix it; connect error; */
            WebBox.appLog(e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
        }

        /* - - */
        return new Pair<OutputStream, InputStream>(outputStream, inputStream);

    }

    public void startBluetoothReader(final String key, final InputStream inputStream) {

        /* start thread */
        Thread readThread = new Thread(new Runnable() {

            public boolean RUN = true;

            @Override
            public void run() {
                try {

                    /* add read buffer */
                    UartGateBuffer uartGateBuffer = new UartGateBuffer(key,0);
                    UartGate.uartInBuffers.put(key, uartGateBuffer);

                    int idx = 0;
                    char ch = (char) 0;
                    char[] chbuff = new char[256];

                    /* thread loop */
                    while (this.RUN) {
                        /* check for char */
                        if (inputStream.available() == 0)
                            continue;
                        /* read stream */
                        ch = (char) inputStream.read();
                        if (ch != '\n') {
                            chbuff[idx++] = ch;
                        } else {
                            uartGateBuffer.addUartMsg(new String(chbuff, 0, --idx));
                            Arrays.fill(chbuff, (char) 0);
                            idx = 0;
                        }
                    }

                    /* close here */
                    uartGateBuffer.close();

                } catch (IOException e) {
                    WebBox.appLog(e.toString());
                }
            }

        });

        /* - - */
        UartGate.uartThreads.put(key, readThread);
        readThread.start();

    }

}
