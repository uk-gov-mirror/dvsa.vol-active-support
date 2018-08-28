package activesupport.driver;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import activesupport.proxy.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;

public class Browser {

    private static String getCurrentWorkingDirectory = System.getProperty("user.dir") + "/src/test/java/activesupport/selenium_drivers/";
    private String operatingSystem = System.getProperty("os.name");
    private String getLocalChromeDriver = System.getenv("CHROMEDRIVER");
    private String getLocalGeckoDriver = System.getenv("FIREFOXDRIVER");
    private static WebDriver driver;

    public static void setDriver(WebDriver driver) {
        Browser.driver = driver;
    }

    public static WebDriver navigate() throws IllegalBrowserException {

        if (driver == null) {
            setDriver(whichBrowser(System.getProperty("browser")));
        }
        return driver;
    }

    private static WebDriver whichBrowser(String browserName) throws IllegalBrowserException {
        browserName = browserName.toLowerCase().trim();

        switch (browserName) {
            case "headless":
                FirefoxOptions options = new FirefoxOptions();
                options.setHeadless(true);
                options.setCapability("javascriptEnabled", true);
                options.setCapability("handleAlerts", true);
                options.setCapability("marionette", true);
                if (driver == null)
                    driver = new FirefoxDriver(options);
                break;
            case "chrome":
                if (driver == null)
                    driver = new ChromeDriver();
                break;
            case "firefox":
                FirefoxOptions optionsFirefox = new FirefoxOptions();
                optionsFirefox.setCapability("marionette", true);
                if (driver == null)
                    driver = new FirefoxDriver(optionsFirefox);
                break;
            case "proxy":
                FirefoxOptions optionProxy = new FirefoxOptions();
                optionProxy.setCapability("Proxy", Proxy.createZapProxyConfigurationForWebDriver("localhost","8090"));
                optionProxy.setCapability("javascriptEnabled", true);
                optionProxy.setCapability("handleAlerts", true);
                optionProxy.setCapability("marionette", true);
                if (driver == null)
                    driver = new FirefoxDriver(optionProxy);
            default:
                throw new IllegalBrowserException();
        }
        return driver;
    }

    public static void quit() throws IllegalBrowserException, MissingDriverException {
        if (driver != null)
            driver.close();
        setDriver(null);
    }

    public static boolean isBrowserOpen() throws IllegalBrowserException {
        boolean isOpen = false;

        if (driver != null) {
            isOpen = true;
        } else {
            isOpen = false;
        }
        return isOpen;
    }
}
