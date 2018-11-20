package activesupport.browsers;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import activesupport.driver.Browser;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

public class BrowserTest {

@Test
    public void chromeTest() throws MissingDriverException, IllegalBrowserException {

             System.setProperty("browser", "headless");
            Browser.navigate().get("http://bbc.co.uk");
            if (Browser.isBrowserOpen()) {
                Browser.quit();
            }
            Browser.navigate().get("http://www.google.co.uk");
        }

    @Test
    public void firefoxTest() throws MissingDriverException, IllegalBrowserException {

        System.setProperty("browser", "firefox");
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
    }

    @Test
    public void proxyTest() throws MissingDriverException, IllegalBrowserException {
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
    }

    @Test
    public void headlessTest() throws MissingDriverException, IllegalBrowserException {

        System.setProperty("browser", "headless");
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
    }

}


