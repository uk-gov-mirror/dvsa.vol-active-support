package activesupport.database;

import activesupport.database.exception.UnsupportedDatabaseDriverException;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static junit.framework.TestCase.assertEquals;


public class DbunitTest {

    @Test
    public void checkColumnValue() throws SQLException, UnsupportedDatabaseDriverException {
       ResultSet resultSet = DBUnit.checkResult("SELECT transport_manager_id FROM OLCS_RDS_OLCSDB.note where id = 1;");
       if(resultSet.next()){
           int columnValue = Integer.parseInt(resultSet.getString("transport_manager_id"));
           assertEquals(14,columnValue);
       }
    }
}
