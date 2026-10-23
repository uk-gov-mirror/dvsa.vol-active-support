package activesupport.database.connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JumpboxDatabaseLoginTest {

    private final List<String> lookups = new ArrayList<>();
    private final BiFunction<String, String, String> secrets = (name, key) -> {
        lookups.add(name + ":" + key);
        return "password-from-" + name;
    };

    @ParameterizedTest
    @CsvSource({
            "dev, DEVAPPDEV-BASE-SM-APPLICATION-API",
            "qa, DEVAPPQA-BASE-SM-APPLICATION-API",
            "int, DEVAPPQA-BASE-SM-APPLICATION-API",
            "reg, DEVAPPREG-BASE-SM-APPLICATION-API",
            "DEV, DEVAPPDEV-BASE-SM-APPLICATION-API"
    })
    public void readsThePasswordFromTheEnvironmentsOwnApplicationSecret(String env, String secretName) {
        String password = JumpboxDatabaseLogin.forEnvironment(env, new Properties(), secrets).password();

        assertEquals("password-from-" + secretName, password);
        assertEquals(List.of(secretName + ":olcs_api_rds_password"), lookups);
    }

    @Test
    public void logsInAsTheApiUser() {
        assertEquals("olcsapi", JumpboxDatabaseLogin.forEnvironment("dev", new Properties(), secrets).username());
    }

    @Test
    public void usesAnExplicitLoginInstead() {
        Properties props = new Properties();
        props.setProperty("db.username", "someone");
        props.setProperty("db.password", "secret");

        JumpboxDatabaseLogin login = JumpboxDatabaseLogin.forEnvironment("dev", props, secrets);

        assertEquals("someone", login.username());
        assertEquals("secret", login.password());
        assertTrue(lookups.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"local", "prep", "unknown"})
    public void hasNoLoginForEnvironmentsWithoutAJumpboxPort(String env) {
        assertNull(JumpboxDatabaseLogin.forEnvironment(env, new Properties(), secrets));
        assertTrue(lookups.isEmpty());
    }
}
