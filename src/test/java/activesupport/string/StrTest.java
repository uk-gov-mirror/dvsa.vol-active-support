package activesupport.string;

import org.junit.Assert;
import org.junit.Test;

public class StrTest {

    @Test
    public void returnsTheMatchingSubstring(){
        String subject = "Today's date is 06-01-2018.";
        String regex = "[\\d\\-\\/]+";

        String expectedSubstring = "06-01-2018";
        String actualSubstring = Str.find(regex, subject);

        Assert.assertEquals(expectedSubstring, actualSubstring);
    }

}
