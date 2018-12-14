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
        ValidatableResponse response = RestUtils.get(url,queryParams(), headers);
        Assert.assertEquals(200,response.extract().statusCode());
    }

    public Map queryParams(){
        Map<String,String> queryParam = new HashMap<>();
        {
            queryParam.put("","");
            queryParam.put("knock","knock");
            queryParam.put("who","is it");
        }
        return queryParam;
    }
}