package activesupport.database.connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DatabaseUrlTest {

    private static final String PARAMS = "/OLCS_RDS_OLCSDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true";

    // Runs without the proxy (CI, AWS Batch) keep their existing URLs, including
    // unknown environments defaulting to QA.
    @ParameterizedTest
    @CsvSource({
            "qa, olcsdb-rds.qa.olcs.dev-dvsacloud.uk:3306",
            "int, olcsdb-rds.int.olcs.dev-dvsacloud.uk:3306",
            "prep, olcsdb-rds.prep.olcs.dev-dvsacloud.uk:3306",
            "local, localhost:3306",
            "dev, olcsdb-rds.qa.olcs.dev-dvsacloud.uk:3306",
            "reg, olcsdb-rds.qa.olcs.dev-dvsacloud.uk:3306"
    })
    public void keepsTheExistingUrlsWithoutTheProxy(String env, String hostAndPort) {
        assertEquals("jdbc:mysql://" + hostAndPort + PARAMS, DatabaseUrl.forEnvironment(env, false));
    }

    @ParameterizedTest
    @CsvSource({
            "dev, 13306",
            "qa, 23306",
            "int, 23306",
            "reg, 33306"
    })
    public void usesTheJumpboxProxyPortWhenTheProxyIsOn(String env, String port) {
        assertEquals("jdbc:mysql://127.0.0.1:" + port + PARAMS, DatabaseUrl.forEnvironment(env, true));
    }

    @Test
    public void keepsTheLocalDatabaseWhenTheProxyIsOn() {
        assertEquals("jdbc:mysql://localhost:3306" + PARAMS, DatabaseUrl.forEnvironment("local", true));
    }
}
