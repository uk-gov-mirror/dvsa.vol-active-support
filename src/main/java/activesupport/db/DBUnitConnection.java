package activesupport.db;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import activesupport.system.Properties;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.util.fileloader.DataFileLoader;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import static org.dbunit.Assertion.assertEquals;



    public class DBUnitConnection {
        private static QueryDataSet dataSet;
        private final static String env = Properties.get("env");
        private final static String dbUsername = System.getenv("DB_USERNAME");
        private final static String dbPassword = System.getenv("DB_PASSWORD");

        private static Connection jdbcConnection() throws Exception {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            Connection dbConnection = DriverManager.getConnection(String.format(
                    "jdbc:mysql://olcsdb-rds.olcs.%S.nonprod.dvsa.aws:3306/?user=%s&password=%s", env, dbUsername, dbPassword));
            return dbConnection;
        }

        public static IDataSet queryDatabase(String sqlQuery, String fileName) {
            IDatabaseConnection connection;
            try {
                connection = new DatabaseConnection(jdbcConnection());
                dataSet = new QueryDataSet(connection);
                dataSet.addTable(String.format(fileName.concat("%s"), Instant.now().getEpochSecond()), sqlQuery);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return dataSet;
        }

        private static File createFolder() throws Exception {
            File folder = new File("src/main/resources/data");
            if (!folder.exists()) {
                try {
                    folder.mkdir();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return folder;
        }

        public static IDataSet readFileAsDataSet(String file) {
            DataFileLoader dataFileLoader = new FlatXmlDataFileLoader();
            IDataSet loadedDataSet = dataFileLoader.load(file);
            return loadedDataSet;
        }

        public static void writeToXml(IDataSet dataSet, String fileName) {
            try {
                FlatXmlDataSet.write(dataSet, new FileOutputStream(createFolder() + "/" + fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void writeToCsv(IDataSet dataSet) {
            try {
                CsvDataSetWriter.write(dataSet, createFolder());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean compareDataSets(IDataSet dataSet, IDataSet dataSet1){
            try {
                assertEquals(dataSet,dataSet1);
            } catch (DatabaseUnitException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

