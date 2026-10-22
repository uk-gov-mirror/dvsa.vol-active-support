package activesupport.proxy;

import com.microsoft.playwright.BrowserType;
import io.restassured.RestAssured;
import io.restassured.specification.ProxySpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.remote.CapabilityType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Routes test traffic through the jumpbox-proxy container (aws-helper), which
 * tunnels to the nonprod environments over SSM, when it is running on this
 * machine. Nothing changes when it is not running, so CI and AWS Batch runs
 * keep their own proxy settings.
 */
public final class JumpboxProxy {

    private static final Logger LOGGER = LogManager.getLogger(JumpboxProxy.class);

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 3128;

    private static final String ADDRESS = HOST + ":" + PORT;
    private static final int PROBE_TIMEOUT_MS = 200;
    private static final Set<String> NONPROD_ENVIRONMENTS = Set.of("dev", "qa", "int", "reg");
    private static final Set<String> PROXY_PROPERTIES = Set.of("http.proxyHost", "https.proxyHost", "httpProxy", "httpsProxy");

    private static Boolean enabled;

    private JumpboxProxy() {
    }

    /**
     * Checks once per run whether the proxy should be used and, if so, sends
     * Java and RestAssured traffic through it.
     *
     * @return whether the proxy is in use
     */
    public static synchronized boolean enableIfRunning() {
        if (enabled == null) {
            enabled = shouldEnable(System.getProperties(), () -> isListening(HOST, PORT));
            if (enabled) {
                routeThroughProxy(System.getProperties());
                LOGGER.info("Using jumpbox-proxy on {} for env {} (-Djumpbox.proxy=off to stop)", ADDRESS, System.getProperty("env", "qa"));
            }
        }
        return enabled;
    }

    static boolean shouldEnable(Properties props, BooleanSupplier listening) {
        if ("off".equalsIgnoreCase(props.getProperty("jumpbox.proxy", ""))) {
            return false;
        }
        String env = props.getProperty("env", "qa").toLowerCase(Locale.ROOT).trim();
        if (!NONPROD_ENVIRONMENTS.contains(env)) {
            return false;
        }
        for (String property : PROXY_PROPERTIES) {
            if (!props.getProperty(property, "").isBlank()) {
                return false;
            }
        }
        return listening.getAsBoolean();
    }

    static boolean isListening(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), PROBE_TIMEOUT_MS);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    static void routeThroughProxy(Properties props) {
        props.setProperty("http.proxyHost", HOST);
        props.setProperty("http.proxyPort", String.valueOf(PORT));
        props.setProperty("https.proxyHost", HOST);
        props.setProperty("https.proxyPort", String.valueOf(PORT));
        RestAssured.proxy = ProxySpecification.host(HOST).withPort(PORT);
    }

    /**
     * Gives a browser running on this machine the proxy. Not for browsers on a
     * Selenium grid, which have their own proxy settings.
     */
    public static void applyTo(MutableCapabilities options) {
        applyTo(options, enableIfRunning());
    }

    static void applyTo(MutableCapabilities options, boolean enabled) {
        if (enabled && options.getCapability(CapabilityType.PROXY) == null) {
            options.setCapability(CapabilityType.PROXY, new Proxy().setHttpProxy(ADDRESS).setSslProxy(ADDRESS));
        }
    }

    public static void applyTo(BrowserType.LaunchOptions options) {
        applyTo(options, enableIfRunning());
    }

    static void applyTo(BrowserType.LaunchOptions options, boolean enabled) {
        if (enabled && options.proxy == null) {
            options.setProxy("http://" + ADDRESS);
        }
    }
}
