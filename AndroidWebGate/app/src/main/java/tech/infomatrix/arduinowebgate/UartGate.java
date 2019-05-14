
package tech.infomatrix.arduinowebgate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.ParcelUuid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import static java.lang.System.in;
import static java.lang.System.out;


public class UartGate {

    public static Context ctx;
    public static UartGate uartGate;
    public static UartGateBuffer uartBuffer;


    public UartGate() {
        UartGate.uartGate = this;
        UartGate.uartBuffer = new UartGateBuffer("bluebuff", 64);
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

    public String listBluetoothDevices(){

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
            return "NoBluetoothFound";
        if(!bluetoothAdapter.isEnabled())
            return "BluetoothNotEnabled";

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        StringBuilder sb = new StringBuilder();
        if(pairedDevices.size() > 0) {
            for (BluetoothDevice d : pairedDevices)
                sb.append(String.format("%s; %s|", d.getName(), d.getAddress()));
        }

        return sb.toString();
    }

    public void bluetoothDeviceInfo(String mac) {
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

        } catch (NullPointerException e) {
            /* todo: try to recover */
            WebBox.appLog(e.toString());
        } catch (Exception e) {
            WebBox.appLog(e.toString());
        }
    }



    private BluetoothDevice bluetoothDeviceByMac(String mac){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice d: pairedDevices){
            if(d.getAddress().equals(mac))
                return d;
        }
        /* - - */
        return null;
    }

    private void startReadBlueUart(final InputStream inputStream) {
        Thread readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    int idx = 0;
                    char ch = (char)0;
                    char[] chbuff = new char[256];

                    while (true) {
                        /* check for char */
                        if (inputStream.available() == 0)
                            continue;
                        /* read stream */
                        ch = (char) inputStream.read();
                        if (ch != '\n') {
                            chbuff[idx++] = ch;
                        } else {
                            idx--;
                            UartGate.uartBuffer.add(new String(chbuff, 0, idx));
                            Arrays.fill(chbuff, (char) 0);
                            idx = 0;
                        }
                    }

                }catch (IOException e){
                    WebBox.appLog(e.toString());
                }
            }
        });
        /* - - */
        readThread.start();
    }
}
