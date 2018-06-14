package activesupport.browsers;

import activesupport.IllegalBrowserException;
import activesupport.MissingDriverException;
import activesupport.driver.Browser;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import static org.junit.Assert.assertTrue;

public class BrowserTest {
    Browser browser = new Browser();

    public static void main(String[] args) throws MissingDriverException, IllegalBrowserException {
        Browser browser = new Browser();
        browser.navigate().get("http://www.google.co.uk");
       assertTrue(browser.navigate().findElement(By.xpath("//*[@name='q']")).isDisplayed());
       browser.quit();
    }
}
