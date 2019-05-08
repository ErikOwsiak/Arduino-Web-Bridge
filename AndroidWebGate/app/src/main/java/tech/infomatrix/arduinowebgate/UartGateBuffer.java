
package tech.infomatrix.arduinowebgate;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;


public class UartGateBuffer {

    private String name;
    private Queue<UartMsg> msgs;
    private int maxsize;

    public UartGateBuffer(String name, int maxsize){
        this.name = name;
        this.msgs = new LinkedList<UartMsg>();
        this.maxsize = maxsize;
    }

    public boolean add(String str){
        if(this.msgs.size() == this.maxsize)
            this.msgs.remove();
        long ts = new Date().getTime();
        this.msgs.add(new UartMsg(ts, str));
        return true;
    }

    public UartMsg read() {
        if (this.msgs.size() > 0)
            return this.msgs.remove();
        return null;
    }

    public UartMsg peek(){
        return this.msgs.peek();
    }

}
