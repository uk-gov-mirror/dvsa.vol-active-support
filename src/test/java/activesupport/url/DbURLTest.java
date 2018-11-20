package activesupport.url;

import activesupport.database.url.DbURL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbURLTest {

    DbURL url = new DbURL();
    String dbUsername = System.setProperty("dbUsername", "testUser");
    String dbPassword = System.setProperty("dbPassword", "testPass");

    @Test
    public void getIntDbURL() {

        String intURL = "jdbc:mysql://olcsdb-rds.olcs.int.prod.dvsa.aws:3306/OLCS_RDS_OLCSDB?user=testUser&password=testPass&useSSL=false";
        String URL = url.getDBUrl("int");
        assertEquals(intURL, URL);
    }

    @Test
    public void getPPDbURL() {
        String intURL = "jdbc:mysql://olcsdb-rds.olcs.pp.prod.dvsa.aws:3306/OLCS_RDS_OLCSDB?user=testUser&password=testPass&useSSL=false";
        String URL = url.getDBUrl("pp");
        assertEquals(intURL, URL);
    }


    @Test
    public void getDevDbURL() {
        String intURL = "jdbc:mysql://olcsdb-rds.olcs.dev.nonprod.dvsa.aws:3306/OLCS_RDS_OLCSDB?user=testUser&password=testPass&useSSL=false";
        String URL = url.getDBUrl("dev");
        assertEquals(intURL, URL);
    }

    @Test
    public void getDADbURL() {
        String intURL = "jdbc:mysql://olcsdb-rds.olcs.da.nonprod.dvsa.aws:3306/OLCS_RDS_OLCSDB?user=testUser&password=testPass&useSSL=false";
        String URL = url.getDBUrl("da");
        assertEquals(intURL, URL);
    }

    @Test
    public void getQADbURL() {
        String intURL = "jdbc:mysql://olcsdb-rds.olcs.qa.nonprod.dvsa.aws:3306/OLCS_RDS_OLCSDB?user=testUser&password=testPass&useSSL=false";
        String URL = url.getDBUrl("qa");
        assertEquals(intURL, URL);
    }
}

