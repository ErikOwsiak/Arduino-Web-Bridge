
package tech.infomatrix.arduinowebgate;


public class ApiCallFeedback {

    public int apiCallCode = 0;
    public String apiCallMsg;
    public String apiCallReturnVal;

    public ApiCallFeedback(int code, String msg, String rval) {
        this.apiCallCode = code;
        this.apiCallMsg = msg;
        this.apiCallReturnVal = rval;
    }

    public String toJsonStr() {
        return String.format("{\"apiReturnCode\": %s, \"apiReturnMsg\": \"%s\", \"apiReturnVal\": \"%s\"}",
                this.apiCallCode, this.apiCallMsg, this.apiCallReturnVal);
    }
}
