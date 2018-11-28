package activesupport.browsers;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import activesupport.driver.Browser;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;

import static org.junit.Assert.assertTrue;

public class BrowserTest {

@Test
    public void chromeTest() throws IllegalBrowserException, MalformedURLException {

             System.setProperty("browser", "headless");
            Browser.navigate().get("http://bbc.co.uk");
            if (Browser.isBrowserOpen()) {
                Browser.quit();
            }
            Browser.navigate().get("http://www.google.co.uk");
        }

    @Test
    public void firefoxTest() throws IllegalBrowserException, MalformedURLException {

        System.setProperty("browser", "firefox");
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
    }

    @Test
    public void proxyTest() throws IllegalBrowserException, MalformedURLException {
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
    }

    @Test
    public void headlessTest() throws IllegalBrowserException, MalformedURLException {

        System.setProperty("browser", "headless");
        Browser.navigate().get("http://bbc.co.uk");
        if (Browser.isBrowserOpen()) {
            Browser.quit();
        }
        Browser.navigate().get("http://www.google.co.uk");
    }
}
