package activesupport.proxy;

import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Turns the jumpbox proxy on before any test runs, so libraries that call
 * RestAssured directly (e.g. vol-api-calls) pick it up too. Registered in
 * META-INF/services.
 */
public class JumpboxProxyListener implements TestExecutionListener {

    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        JumpboxProxy.enableIfRunning();
    }
}
