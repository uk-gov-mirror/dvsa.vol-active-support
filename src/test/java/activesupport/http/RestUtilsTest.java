package activesupport.http;

import io.restassured.response.ValidatableResponse;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RestUtilsTest {

    @Test
    public void getCall(){
        Map<String,String> headers = new HashMap<>();
        {
            headers.put("test","header");
        }
        String url = "https://data.gov.uk/api";
        ValidatableResponse response = RestUtils.get(url, headers);
        Assert.assertEquals(200,response.extract().statusCode());
    }
}