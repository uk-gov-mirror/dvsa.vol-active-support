package activesupport.http;

import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RestUtils {

    private static ValidatableResponse response;

    public static ValidatableResponse post(@NotNull Object requestBody, @NotNull URL serviceEndPoint, @NotNull Map<String, String> headers) {
        return post(requestBody, serviceEndPoint.toString(), headers);
    }

    public static ValidatableResponse post(@NotNull Object requestBody, @NotNull String serviceEndPoint, @NotNull Map<String, String> headers) {
        response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(requestBody)
                .when().config(RestAssuredConfig.config().sslConfig(new SSLConfig().relaxedHTTPSValidation().allowAllHostnames()))
                .post(serviceEndPoint)
                .then();
        return response;
    }

    public static ValidatableResponse put(@NotNull Object requestBody, @NotNull URL serviceEndPoint, @NotNull Map<String, String> headers) {
        return put(requestBody, serviceEndPoint.toString(), headers);
    }

    public static ValidatableResponse put(@NotNull Object requestBody, @NotNull String serviceEndPoint, @NotNull Map<String, String> headers) {
        response = given()
                .log().all()
                .contentType(ContentType.JSON)
                .headers(headers)
                .body(requestBody)
                .when().config(RestAssuredConfig.config().sslConfig(new SSLConfig().relaxedHTTPSValidation().allowAllHostnames()))
                .put(serviceEndPoint)
                .then();
        return response;
    }

    public static ValidatableResponse get(@NotNull URL serviceEndPoint, @NotNull Map<String, String> headers) {
        return get(serviceEndPoint.toString(), headers);
    }

    public static ValidatableResponse get(@NotNull String serviceEndPoint, @NotNull Map<String, String> headers) {
        response = given()
                .log().all()
                .headers(headers)
                .when().config(RestAssuredConfig.config().sslConfig(new SSLConfig().relaxedHTTPSValidation().allowAllHostnames()))
                .get(serviceEndPoint)
                .then();
        return response;
    }

    public static void UrlEncoding(boolean enabled) {
        RestAssured.urlEncodingEnabled = enabled;
    }

}