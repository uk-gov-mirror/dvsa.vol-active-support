package activesupport.driver;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import activesupport.proxy.Proxy;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Browser {

    private static String getCurrentWorkingDirectory = System.getProperty("user.dir") + "/src/test/java/activesupport/selenium_drivers/";
    private String operatingSystem = System.getProperty("os.name");
    private String getLocalChromeDriver = System.getenv("CHROMEDRIVER");
    private String getLocalGeckoDriver = System.getenv("FIREFOXDRIVER");
    private static WebDriver driver;

    public static void setDriver(WebDriver driver) {
        Browser.driver = driver;
    }

    public static WebDriver navigate() throws IllegalBrowserException, MalformedURLException {

        if (driver == null) {
            setDriver(whichBrowser(System.getProperty("browser")));
        }
        return driver;
    }

    private static WebDriver whichBrowser(String browserName) throws IllegalBrowserException, MalformedURLException {
        browserName = browserName.toLowerCase().trim();
        FirefoxOptions options = new FirefoxOptions();
        URL url = new URL("http://localhost:4444/wd/hub");
        switch (browserName) {
            case "headless":
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
                optionProxy.setProxy(Proxy.createZapProxyConfigurationForWebDriver("localhost", "8090"));
                if (driver == null)
                    driver = new FirefoxDriver(optionProxy);
                break;
            case "chrome-grid":
                ChromeOptions capabilities = new ChromeOptions();
                capabilities.setCapability(CapabilityType.BROWSER_NAME, BrowserType.CHROME);
                capabilities.setCapability(CapabilityType.PLATFORM, Platform.LINUX);

                driver = new RemoteWebDriver(url, capabilities);
                break;
            case "firefox-grid":
                optionsFirefox = new FirefoxOptions();
                optionsFirefox.setCapability("marionette", true);
                optionsFirefox.setCapability(CapabilityType.PLATFORM, Platform.LINUX);

                driver = new RemoteWebDriver(url, optionsFirefox);
                break;
            case "ie-grid":
                InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions();
                internetExplorerOptions.setCapability(CapabilityType.PLATFORM, Platform.LINUX);

                driver = new RemoteWebDriver(url, internetExplorerOptions);
                break;
            case "safari-grid":
                SafariOptions safariOptions = new SafariOptions();
                safariOptions.setCapability(CapabilityType.PLATFORM, Platform.LINUX);

                driver = new RemoteWebDriver(url, safariOptions);
                break;
            default:
                throw new IllegalBrowserException();
        }
        return driver;
    }

    public static void quit() {
        if (driver != null)
            driver.close();
        setDriver(null);
    }

    public static boolean isBrowserOpen() {
        boolean isOpen;

        isOpen = driver != null;
        return isOpen;
    }
}
