package activesupport.database;

import activesupport.MissingRequiredArgument;
import activesupport.database.credential.DatabaseCredentialType;
import activesupport.system.Properties;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.csv.CsvDataSetWriter;
import org.dbunit.dataset.csv.CsvURLDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
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
import java.time.Instant;

import static activesupport.file.Files.createFolder;
import static org.dbunit.Assertion.assertEquals;

public class DBUnit {

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
        IDatabaseConnection dbUnitConnection;
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

    public static boolean equals(IDataSet dataSet, IDataSet dataSet1, @NotNull String table){
        boolean equals = true;

        try {
            assertEquals(dataSet.getTable(table),dataSet1.getTable(table));
        } catch (DatabaseUnitException e) {
            equals = false;
        }

        return equals;
    }

    private static String loadDBCredential(@NotNull DatabaseCredentialType databaseCredentialType) throws MissingRequiredArgument {
        return Properties.get(databaseCredentialType.toString(), true);
    }

}
