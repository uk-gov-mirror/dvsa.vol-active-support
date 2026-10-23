package activesupport.database.connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Resolves the JDBC URL for an environment. Kept apart from
 * DatabaseConnectionManager, whose static initialiser loads credentials from
 * Secrets Manager.
 */
final class DatabaseUrl {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseUrl.class);

    private static final String DATABASE = "/OLCS_RDS_OLCSDB";
    private static final String PARAMS = "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&autoReconnect=true";

    private DatabaseUrl() {
    }

    /**
     * @param environment the target environment
     * @param viaJumpbox  whether the jumpbox-proxy container is in use, in which
     *                    case nonprod databases are reached on its local ports
     */
    static String forEnvironment(String environment, boolean viaJumpbox) {
        String env = environment.toLowerCase().trim();
        JumpboxDatabase jumpboxDatabase = JumpboxDatabase.forEnvironment(env);
        if (viaJumpbox && jumpboxDatabase != null) {
            return "jdbc:mysql://127.0.0.1:" + jumpboxDatabase.port + DATABASE + PARAMS;
        }

        return switch (env) {
            case "qa", "qualityassurance" ->
                    "jdbc:mysql://olcsdb-rds.qa.olcs.dev-dvsacloud.uk:3306" + DATABASE + PARAMS;
            case "int", "integration" ->
                    "jdbc:mysql://olcsdb-rds.int.olcs.dev-dvsacloud.uk:3306" + DATABASE + PARAMS;
            case "prep", "preproduction" ->
                    "jdbc:mysql://olcsdb-rds.prep.olcs.dev-dvsacloud.uk:3306" + DATABASE + PARAMS;
            case "local", "localhost" -> "jdbc:mysql://localhost:3306" + DATABASE + PARAMS;
            default -> {
                LOGGER.warn("Unknown environment '{}'. Defaulting to QA", environment);
                yield "jdbc:mysql://olcsdb-rds.qa.olcs.dev-dvsacloud.uk:3306" + DATABASE + PARAMS;
            }
        };
    }
}
