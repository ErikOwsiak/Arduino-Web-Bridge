
package tech.infomatrix.arduinowebgate;

import java.net.Socket;


public class WebBoxThread implements Runnable {

    private Socket reqSocket = null;

    public WebBoxThread(Socket soc){
        this.reqSocket = soc;
    }

    @Override
    public void run(){

    }

}

