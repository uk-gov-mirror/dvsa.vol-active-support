package activesupport.driver;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;

public class Browser {

    private static String getCurrentWorkingDirectory = System.getProperty("user.dir") + "/src/test/java/activesupport/selenium_drivers/";
    private String operatingSystem = System.getProperty("os.name");
    private String getLocalChromeDriver = System.getenv("CHROMEDRIVER");
    private String getLocalGeckoDriver = System.getenv("FIREFOXDRIVER");
    private static WebDriver driver;
    private static String browserName = System.getProperty("browser");

    public void selectBrowser() throws IllegalBrowserException, MissingDriverException {
        String windows = ".exe";
        String chrome = "chromedriver";
        String firefoxDriver = "geckodriver";
        try {
            if (operatingSystem.contains("Windows") && browserName.contains("chrome")) {
                checkFileExists(windows, chrome, "webdriver.chrome.driver");
            }  if (!operatingSystem.contains("Windows") && !browserName.contains("chrome")) {
                checkFileExists(windows, firefoxDriver, "webdriver.firefox.marionette");
            }
    } catch (Exception e) {
        e.printStackTrace();
        throw new MissingDriverException();
    }

    }

    public WebDriver navigate() throws IllegalBrowserException {
      driver =  whichBrowser(browserName);
      setDriver(driver);
      return driver;
    }

    public static void setDriver(WebDriver driver) {
        Browser.driver = driver;
    }

    private void checkFileExists(String windows, String browserName, String driverProperty) {
        File file = new File(getCurrentWorkingDirectory + browserName + windows);
        if (file.exists() &&(browserName.equals("chrome")))
        {
            createDriverPath(driverProperty, getCurrentWorkingDirectory, browserName, windows);
        } else {
            createDriverPath(driverProperty, getLocalChromeDriver, null, null);
        }
        if (file.exists() &&(browserName.equals("firefox"))) {
            createDriverPath(driverProperty, getCurrentWorkingDirectory, browserName, windows);
        } else {
            createDriverPath(driverProperty, getLocalGeckoDriver, null, null);
        }
    }


    private static String createDriverPath(String driverProperty, String path, String extention, String extention2){
      return  System.setProperty(driverProperty, path + extention + extention2);
    }

    private static WebDriver whichBrowser(String browserName) throws IllegalBrowserException {
        browserName = browserName.toLowerCase().trim();

        switch (browserName) {
            case "headless":
                ChromeOptions optionsChrome = new ChromeOptions();
                optionsChrome.setHeadless(true);
                if (driver == null)
                    driver = new ChromeDriver(optionsChrome);
            case "chrome":
                if (driver == null)
                    driver = new ChromeDriver();
            case "firefox":
                FirefoxOptions optionsFirefox = new FirefoxOptions();
               optionsFirefox.setCapability("marionette", false);
                if (driver == null)
                    driver = new FirefoxDriver(optionsFirefox);
                break;
            default:
                throw new IllegalBrowserException();
        }
        return driver;
    }

    public void quit() throws IllegalBrowserException, MissingDriverException {
        if (navigate() != null)
            navigate().quit();
    }
}
