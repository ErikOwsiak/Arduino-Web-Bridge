
package tech.infomatrix.arduinowebgate;


public class MimeTypes {

    final  public static String CT_HTML = "text/html";
    final  public static String CT_JSON = "text/json";
    final  public static String CT_CSS = "text/css";
    final  public static String CT_JPG = "image/jpeg";
    final  public static String CT_PNG = "image/png";
    final  public static String CT_SVG = "image/svg";
    final  public static String CT_JS = "text/javascript";

    public static String fromExt(String ext) {
        switch (ext) {
            case ".html":
            case ".htm":
                return CT_HTML;
            case ".json":
                return CT_JSON;
            case ".css":
                return CT_CSS;
            case ".jpg":
                return CT_JPG;
            case ".png":
                return CT_PNG;
            case ".svg":
                return CT_SVG;
            case ".js":
                return CT_JS;
            default:
                return null;
        }
    }
}
