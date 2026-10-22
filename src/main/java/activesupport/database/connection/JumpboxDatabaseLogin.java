package activesupport.database.connection;

import java.util.Properties;
import java.util.function.BiFunction;

/**
 * The database login for a run through the jumpbox-proxy container: the API's
 * own user for that environment, unless -Ddb.username / -Ddb.password are given.
 */
record JumpboxDatabaseLogin(String username, String password) {

    private static final String API_USER = "olcsapi";
    private static final String PASSWORD_KEY = "olcs_api_rds_password";

    /**
     * @param secrets looks up a key in a Secrets Manager secret, as (secret name, key) -> value
     * @return the login, or null if the jumpbox has no database for the environment
     */
    static JumpboxDatabaseLogin forEnvironment(String environment, Properties props, BiFunction<String, String, String> secrets) {
        JumpboxDatabase database = JumpboxDatabase.forEnvironment(environment);
        if (database == null) {
            return null;
        }
        String username = props.getProperty("db.username", API_USER);
        String password = props.getProperty("db.password");
        if (password == null) {
            password = secrets.apply(database.secretName, PASSWORD_KEY);
        }
        return new JumpboxDatabaseLogin(username, password);
    }
}
