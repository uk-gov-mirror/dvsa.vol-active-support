package activesupport;

public class IllegalBrowserException extends Exception {
    public IllegalBrowserException() {
        super("[ERROR] incorrect browser name." + "/n" + "[OPTIONS] 1) Chrome, 2) Headless, 3) Firefox 4)Proxy ");
    }
}