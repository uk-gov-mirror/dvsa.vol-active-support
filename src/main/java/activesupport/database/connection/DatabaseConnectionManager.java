package activesupport.database.connection;

import activesupport.aws.s3.SecretsManager;
import activesupport.database.Driver;
import activesupport.database.exception.UnsupportedDatabaseDriverException;
import activesupport.proxy.JumpboxProxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Consolidated database connection manager for all VOL testing projects.
 * Provides unified database connection handling with environment-based URL resolution
 * and credential management via AWS Secrets Manager.
 */
public class DatabaseConnectionManager {
    
    private static final Logger LOGGER = LogManager.getLogger(DatabaseConnectionManager.class);
    
    private static String dbUsername;
    private static String dbPassword;
    private static Connection cachedConnection;
    
    static {
        try {
            dbUsername = SecretsManager.getSecretValue("dbUsername");
            dbPassword = SecretsManager.getSecretValue("dbPassword");
            LOGGER.info("Database credentials loaded from Secrets Manager");
        } catch (Exception e) {
            LOGGER.error("Failed to get DB credentials from Secrets Manager", e);
            // Fallback for local development
            dbUsername = System.getProperty("db.username", "olcs");
            dbPassword = System.getProperty("db.password", "olcs");
            LOGGER.warn("Using fallback database credentials");
        }
    }
    
    /**
     * Gets a database connection for the specified environment using MySQL driver.
     * 
     * @param environment The target environment (qa, int, prep, etc.)
     * @return Database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection(String environment) throws SQLException {
        return getConnection(environment, Driver.MYSQL);
    }
    
    /**
     * Gets a database connection for the specified environment and driver.
     * 
     * @param environment The target environment (qa, int, prep, etc.)
     * @param driver The database driver to use
     * @return Database connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection(String environment, Driver driver) throws SQLException {
        try {
            // Load the driver class
            Class.forName(driver.toString()).getDeclaredConstructor().newInstance();
            
            String dbUrl = buildDatabaseUrl(environment);
            LOGGER.debug("Connecting to database: {}", maskUrl(dbUrl));

            // Through the jumpbox each environment uses its own login; the shared
            // test runner login only works for QA.
            JumpboxDatabaseLogin login = JumpboxProxy.enableIfRunning()
                    ? JumpboxDatabaseLogin.forEnvironment(environment, System.getProperties(), SecretsManager::getSecretValue)
                    : null;
            if (login != null) {
                return DriverManager.getConnection(dbUrl, login.username(), login.password());
            }
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            
        } catch (Exception e) {
            LOGGER.error("Failed to establish database connection for environment: {}", environment, e);
            throw new SQLException("Database connection failed", e);
        }
    }
    
    /**
     * Gets a cached database connection for the current environment.
     * Creates new connection if cache is null or closed.
     * 
     * @return Cached database connection
     * @throws SQLException if connection fails
     */
    public static Connection getCachedConnection() throws SQLException {
        String env = System.getProperty("env", "qa");
        return getCachedConnection(env);
    }
    
    /**
     * Gets a cached database connection for the specified environment.
     * Creates new connection if cache is null or closed.
     * 
     * @param environment The target environment
     * @return Cached database connection
     * @throws SQLException if connection fails
     */
    public static Connection getCachedConnection(String environment) throws SQLException {
        if (cachedConnection == null || cachedConnection.isClosed()) {
            cachedConnection = getConnection(environment);
            LOGGER.info("Created new cached database connection for environment: {}", environment);
        }
        return cachedConnection;
    }
    
    /**
     * Builds the database URL for the specified environment.
     * 
     * @param environment The target environment
     * @return Complete JDBC URL
     */
    public static String buildDatabaseUrl(String environment) {
        return DatabaseUrl.forEnvironment(environment, JumpboxProxy.enableIfRunning());
    }
    
    /**
     * Tests the database connection for the specified environment.
     * 
     * @param environment The target environment
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection(String environment) {
        try (Connection connection = getConnection(environment)) {
            LOGGER.info("✅ Database connection test successful for environment: {}", environment);
            return true;
        } catch (SQLException e) {
            LOGGER.error("❌ Database connection test failed for environment: {}", environment, e);
            return false;
        }
    }
    
    /**
     * Closes the cached connection if it exists.
     */
    public static void closeCachedConnection() {
        if (cachedConnection != null) {
            try {
                cachedConnection.close();
                LOGGER.info("Cached database connection closed");
            } catch (SQLException e) {
                LOGGER.warn("Failed to close cached connection", e);
            } finally {
                cachedConnection = null;
            }
        }
    }
    
    /**
     * Gets the database username from Secrets Manager.
     * 
     * @return Database username
     */
    public static String getDatabaseUsername() {
        return dbUsername;
    }
    
    /**
     * Gets the database password from Secrets Manager.
     * 
     * @return Database password
     */
    public static String getDatabasePassword() {
        return dbPassword;
    }
    
    /**
     * Masks sensitive parts of the database URL for logging.
     * 
     * @param url The database URL
     * @return Masked URL
     */
    private static String maskUrl(String url) {
        if (url == null) return null;
        // Mask password if present in URL
        return url.replaceAll("password=[^&]*", "password=***");
    }
}