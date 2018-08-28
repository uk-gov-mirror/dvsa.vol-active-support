package activesupport.browsers;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import activesupport.driver.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

public class BrowserTest {


    public static void main(String[] args) throws MissingDriverException, IllegalBrowserException, InterruptedException {

        System.setProperty("browser","firefox");
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
        }
}
