package activesupport.url;

import activesupport.database.url.DbURL;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DbURLTest {

    DbURL url = new DbURL();

    @Test
    public void getIntDbURL() {
        String intURL = "https://ssap1.olcs.int.prod.dvsa.aws/";
        String URL = url.getDBUrl("INT");
        assertEquals(intURL, URL);
    }

    @Test
    public void getPPDbURL() {
        String intURL = "https://ssap1.olcs.pp.prod.dvsa.aws/";
        String URL = url.getDBUrl("pp");
        assertEquals(intURL, URL);
    }


    @Test
    public void getDevDbURL() {
        String intURL = "https://ssap1.olcs.dev.nonprod.dvsa.aws/";
        String URL = url.getDBUrl("dev");
        assertEquals(intURL, URL);
    }

    @Test
    public void getDADbURL() {
        String intURL = "https://ssap1.olcs.da.nonprod.dvsa.aws/";
        String URL = url.getDBUrl("da");
        assertEquals(intURL, URL);
    }

    @Test
    public void getQADbURL() {
        String intURL = "https://ssap1.olcs.qa.nonprod.dvsa.aws/";
        String URL = url.getDBUrl("QA");
        assertEquals(intURL, URL);
    }
}

