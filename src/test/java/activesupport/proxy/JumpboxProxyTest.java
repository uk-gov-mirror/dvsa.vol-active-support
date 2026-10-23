package activesupport.proxy;

import com.microsoft.playwright.BrowserType;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JumpboxProxyTest {

    @AfterEach
    public void resetRestAssured() {
        RestAssured.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {"dev", "qa", "int", "reg", "DEV"})
    public void enabledForNonprodEnvironmentsWhenTheContainerIsListening(String env) {
        assertTrue(JumpboxProxy.shouldEnable(properties("env", env), () -> true));
    }

    @Test
    public void treatsAMissingEnvAsQa() {
        assertTrue(JumpboxProxy.shouldEnable(new Properties(), () -> true));
    }

    @Test
    public void disabledWhenNothingIsListening() {
        assertFalse(JumpboxProxy.shouldEnable(properties("env", "dev"), () -> false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "localhost", "prep", "prod"})
    public void disabledForEnvironmentsTheJumpboxCannotReach(String env) {
        assertFalse(JumpboxProxy.shouldEnable(properties("env", env), () -> true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"http.proxyHost", "https.proxyHost", "httpProxy", "httpsProxy"})
    public void disabledWhenAProxyIsAlreadyConfigured(String property) {
        Properties props = properties("env", "dev");
        props.setProperty(property, "squid.example:3128");

        assertFalse(JumpboxProxy.shouldEnable(props, () -> true));
    }

    @Test
    public void disabledWhenSwitchedOff() {
        Properties props = properties("env", "dev");
        props.setProperty("jumpbox.proxy", "off");

        assertFalse(JumpboxProxy.shouldEnable(props, () -> true));
    }

    @Test
    public void detectsAListeningPort() throws IOException {
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            assertTrue(JumpboxProxy.isListening("127.0.0.1", server.getLocalPort()));
        }
    }

    @Test
    public void detectsAClosedPort() throws IOException {
        int port;
        try (ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            port = server.getLocalPort();
        }

        assertFalse(JumpboxProxy.isListening("127.0.0.1", port));
    }

    @Test
    public void routesJavaAndRestAssuredTrafficThroughTheProxy() {
        Properties props = new Properties();

        JumpboxProxy.routeThroughProxy(props);

        assertEquals("127.0.0.1", props.getProperty("http.proxyHost"));
        assertEquals("3128", props.getProperty("http.proxyPort"));
        assertEquals("127.0.0.1", props.getProperty("https.proxyHost"));
        assertEquals("3128", props.getProperty("https.proxyPort"));
        assertEquals("127.0.0.1", RestAssured.proxy.getHost());
        assertEquals(3128, RestAssured.proxy.getPort());
    }

    @Test
    public void givesALocalBrowserTheProxy() {
        ChromeOptions options = new ChromeOptions();

        JumpboxProxy.applyTo(options, true);

        Proxy proxy = (Proxy) options.getCapability(CapabilityType.PROXY);
        assertEquals("127.0.0.1:3128", proxy.getHttpProxy());
        assertEquals("127.0.0.1:3128", proxy.getSslProxy());
    }

    @Test
    public void leavesBrowserOptionsAloneWhenDisabled() {
        ChromeOptions options = new ChromeOptions();

        JumpboxProxy.applyTo(options, false);

        assertNull(options.getCapability(CapabilityType.PROXY));
    }

    @Test
    public void keepsABrowserProxyThatIsAlreadySet() {
        ChromeOptions options = new ChromeOptions();
        options.setProxy(new Proxy().setSslProxy("squid.example:3128"));

        JumpboxProxy.applyTo(options, true);

        assertEquals("squid.example:3128", ((Proxy) options.getCapability(CapabilityType.PROXY)).getSslProxy());
    }

    @Test
    public void givesPlaywrightTheProxy() {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();

        JumpboxProxy.applyTo(options, true);

        assertEquals("http://127.0.0.1:3128", options.proxy.server);
    }

    @Test
    public void leavesPlaywrightAloneWhenDisabled() {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();

        JumpboxProxy.applyTo(options, false);

        assertNull(options.proxy);
    }

    @Test
    public void isRegisteredToRunBeforeJUnitTests() {
        boolean registered = ServiceLoader.load(TestExecutionListener.class).stream()
                .anyMatch(provider -> provider.type() == JumpboxProxyListener.class);

        assertTrue(registered);
    }

    private static Properties properties(String key, String value) {
        Properties props = new Properties();
        props.setProperty(key, value);
        return props;
    }
}
