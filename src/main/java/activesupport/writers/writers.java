package activesupport.writers;

import activesupport.enums.FileType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class writers {

    public static void writeToFile(ArrayList<String> attributeStringArray, String header1 , String header2, String header3, String fileName, FileType fileType) throws IOException {
        attributeStringArray.add(header1);
        attributeStringArray.add(header2);
        attributeStringArray.add(header3);

        BufferedWriter br = new BufferedWriter(new FileWriter (fileName.concat("."+ fileType)));
        StringBuilder sb = new StringBuilder();

        for (String element2 : attributeStringArray) {
            sb.append(element2);
            sb.append(",");
        }
        br.newLine();
        br.write(sb.toString());
        br.close();


    }
}
