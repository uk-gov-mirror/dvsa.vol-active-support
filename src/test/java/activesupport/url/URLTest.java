package activesupport.url;

import org.junit.Assert;
import org.junit.Test;

public class URLTest {


    @Test
    public void extractsHTTPScheme(){
        String testURL = "http://example.co.uk";
        String expectedScheme = "http";
        String actualScheme = URL.extractScheme(testURL);

        Assert.assertEquals(expectedScheme, actualScheme);
    }

    @Test
    public void extractsHTTPSSchemes(){
        String testURL = "https://example.co.uk";
        String expectedScheme = "https";
        String actualScheme = URL.extractScheme(testURL);

        Assert.assertEquals(expectedScheme, actualScheme);
    }

    @Test
    public void extractsDomain(){
        String testURL = "https://example.com";
        String expectedDomain = "example.com";
        String actualDomain = URL.extractDomain(testURL);

        Assert.assertEquals(expectedDomain, actualDomain);
    }

    @Test
    public void extractsPath(){
        String testURL = "https://example.co.uk/resource/path";
        String expectedPath = "resource/path/";
        String actualPath = URL.extractPath(testURL);

        Assert.assertEquals(expectedPath, actualPath);
    }

}
