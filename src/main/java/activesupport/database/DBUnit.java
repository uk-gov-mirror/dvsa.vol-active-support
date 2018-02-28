package activesupport.database;

import activesupport.MissingRequiredArgument;
import activesupport.database.credential.DatabaseCredentialType;
import activesupport.system.Properties;
import org.dbunit.Assertion;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.database.search.TablesDependencyHelper;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.csv.CsvURLDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.util.TableFormatter;
import org.dbunit.util.fileloader.CsvDataFileLoader;
import org.dbunit.util.fileloader.DataFileLoader;
import org.dbunit.util.fileloader.FlatXmlDataFileLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static activesupport.file.Files.createFolder;

public class DBUnit {
    private static IDatabaseConnection dbUnitConnection;
    private static IDataSet dataSet;

    public static IDataSet readCSV(@NotNull File file) throws DataSetException, MalformedURLException {
        return new CsvURLDataSet(file.toURI().toURL());
    }

    public static Connection JDBConnection(@NotNull Driver driver) throws Exception {
        Class.forName(driver.toString()).newInstance();

        Connection dbConnection = DriverManager.getConnection(String.format(
                "jdbc:mysql://olcsdb-rds.olcs.%S.nonprod.dvsa.aws:3306/?user=%s&password=%s&useSSL=false",
                Properties.get("env"),
                loadDBCredential(DatabaseCredentialType.USERNAME),
                loadDBCredential(DatabaseCredentialType.PASSWORD))
        );

        return dbConnection;
    }

    public static IDataSet queryDatabase(@NotNull String query, @NotNull String fileName) throws Exception {
        QueryDataSet dataSet;

        dbUnitConnection = new DatabaseConnection(JDBConnection(Driver.MYSQL));
        dataSet = new QueryDataSet(dbUnitConnection);
        dataSet.addTable(fileName, query);
        return dataSet;
    }

    public static IDataSet readXMLFileAsDataSet(String file) {
        DataFileLoader dataFileLoader = new FlatXmlDataFileLoader();
        IDataSet loadedDataSet = dataFileLoader.load(file);
        return loadedDataSet;
    }

    public static IDataSet readCSVFileAsDataSet(String file) {
        DataFileLoader dataFileLoader = new CsvDataFileLoader();
        IDataSet loadedDataset = dataFileLoader.load(file);
        return loadedDataset;
    }

    public static void writeToXml(IDataSet dataSet, @NotNull String directory, @NotNull String fileName) throws IOException, DataSetException {
        FlatXmlDataSet.write(dataSet, new FileOutputStream(createFolder(directory) + "/" + fileName));
    }

    public static void writeToCsv(@NotNull IDataSet dataSet, @NotNull String path) throws DataSetException {
        CsvDataSetWriter.write(dataSet, createFolder(path));
    }

    public static boolean equals(IDataSet dataSet, IDataSet dataSet1, @NotNull String table) {
        boolean equals = true;

        try {
            Assertion.assertEquals(dataSet.getTable(table), dataSet1.getTable(table));
        } catch (DatabaseUnitException e) {
            equals = false;
        }

        return equals;
    }

    private static String loadDBCredential(@NotNull DatabaseCredentialType databaseCredentialType) throws MissingRequiredArgument {
        return Properties.get(databaseCredentialType.toString(), true);
    }

    private static String getDiffBetweenTables(@NotNull ITable expectedTable, @NotNull ITable actualTable) throws DataSetException {
        TableFormatter tableFormatter = new TableFormatter();
        String newline = System.getProperty("line.separator");
        StringBuilder differences = new StringBuilder();
        String.valueOf(differences.append("---Expected Table---" + newline));
        differences.append(tableFormatter.format(expectedTable) + newline);
        String.valueOf(differences.append("---Actual Table---" + newline));
        differences.append(tableFormatter.format(actualTable) + newline);

        return differences.toString();
    }

    public static IDataSet exportFullDataSet() throws Exception {
        dbUnitConnection = new DatabaseConnection(JDBConnection(Driver.MYSQL));
        dataSet = dbUnitConnection.createDataSet();
        return dataSet;
    }

    // dependent tables database export: export table X and all tables that
    // have a PK which is a FK on X, in the right order for insertion
    public static IDataSet dependentTableExport(@NotNull String rootTableName) throws Exception {
        try {
            String[] depTableNames =
                    TablesDependencyHelper.getAllDependentTables(dbUnitConnection, rootTableName);
            dataSet = dbUnitConnection.createDataSet(depTableNames);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataSet;
    }

    public static void assertEquals(@NotNull IDataSet expectedDataSet, @NotNull IDataSet actualDataSet, @NotNull String table) throws Exception {
        try {
            Assertion.assertEquals(expectedDataSet.getTable(table), expectedDataSet.getTable(table));
        } catch (AssertionError e) {
            throw new DatabaseUnitException(getDiffBetweenTables(expectedDataSet.getTable(table), expectedDataSet.getTable(table)), e);
        }
    }
}

