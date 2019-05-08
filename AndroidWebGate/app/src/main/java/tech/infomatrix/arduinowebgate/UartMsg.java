
package tech.infomatrix.arduinowebgate;

public class UartMsg {

    private long ts;
    private String msg;

    public UartMsg(long ts, String msg){
        this.ts = ts;
        this.msg = msg;
    }

    public String toString() {
        return String.format("%s; %s", this.ts, this.msg);
    }

}
