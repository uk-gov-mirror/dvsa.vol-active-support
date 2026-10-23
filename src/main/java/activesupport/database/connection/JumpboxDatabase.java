package activesupport.database.connection;

import java.util.Locale;

/**
 * The nonprod databases reachable through the jumpbox-proxy container: the
 * local port it publishes for each writer, and the environment's own
 * application secret holding the API's database password.
 */
enum JumpboxDatabase {
    DEV(13306, "DEVAPPDEV-BASE-SM-APPLICATION-API"),
    // qa runs on the int Aurora cluster
    QA(23306, "DEVAPPQA-BASE-SM-APPLICATION-API"),
    REG(33306, "DEVAPPREG-BASE-SM-APPLICATION-API");

    final int port;
    final String secretName;

    JumpboxDatabase(int port, String secretName) {
        this.port = port;
        this.secretName = secretName;
    }

    /**
     * @return the database for the environment, or null if the jumpbox has none
     */
    static JumpboxDatabase forEnvironment(String environment) {
        return switch (environment.toLowerCase(Locale.ROOT).trim()) {
            case "dev" -> DEV;
            case "qa", "qualityassurance", "int", "integration" -> QA;
            case "reg" -> REG;
            default -> null;
        };
    }
}
