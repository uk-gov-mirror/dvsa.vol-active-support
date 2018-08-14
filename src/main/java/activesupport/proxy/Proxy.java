package activesupport.proxy;

public class Proxy {

    public static org.openqa.selenium.Proxy createZapProxyConfigurationForWebDriver(String iPAddress, String portNo) {
        org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();

        proxy.setHttpProxy(String.format("%s:%s", iPAddress, portNo));
        proxy.setFtpProxy(String.format("%s:%s", iPAddress, portNo));
        proxy.setSslProxy(String.format("%s:%s", iPAddress, portNo));
        return proxy;
    }

}
