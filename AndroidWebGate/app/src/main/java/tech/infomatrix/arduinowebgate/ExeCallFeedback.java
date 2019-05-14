
package tech.infomatrix.arduinowebgate;


public class ExeCallFeedback {

    public int callCode = 0;
    public String callMsg;
    public String callReturnVal;

    public ExeCallFeedback(int code, String msg, String rval) {
        this.callCode = code;
        this.callMsg = msg;
        this.callReturnVal = rval;
    }

    public String toJsonStr() {
        return String.format("{\"returnCode\": %s, \"returnMsg\": \"%s\", \"returnVal\": \"%s\"}",
                this.callCode, this.callMsg, this.callReturnVal);
    }
}
