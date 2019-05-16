
package tech.infomatrix.arduinowebgate;

import java.util.Date;

public class UartMsg {

    private long ts;
    private String msg;

    public UartMsg(String msg) {
        this.ts = new Date().getTime();
        this.msg = msg;
    }

    public String toString() {
        return String.format("%s; %s", this.ts, this.msg);
    }

}
