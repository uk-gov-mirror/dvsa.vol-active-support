package activesupport.aws.s3.util;

import activesupport.system.Properties;
import activesupport.system.out.Output;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class Util {

    public static String s3TempPasswordObjectName(@NotNull String emailAddress) {
        String S3SanitiseObjectName = emailAddress.replaceAll("[@\\.]","");
        return S3SanitiseObjectName + "__Your_temporary_password";
    }

    public static String s3Path(@NotNull String S3ObjectName){
        String env = System.getProperty("env");

        if(env.isEmpty()){
            throw new IllegalArgumentException(
                    Output.printColoredLog("[ERROR] SYSTEM PROPERTY: env is not set")
            );
        }

        return String.format("olcs.%s.nonprod.dvsa.aws/email/%s", env, S3ObjectName);
    }

    /**
     * Adds to system properties the properties specified at properties/config.properties
     * */
    public static void loadConfigProperties(){
        String pathToProperties = "properties/config.properties";
        loadProperties(pathToProperties);
    }

    /**
     * Adds the properties of the property file at the specified path to system properties.
     * @param path path to a properties file from project root.
     * */
    public static void loadProperties(@NotNull String path){
        // Adds properties specified in properties/config.properties into system properties
        if(java.nio.file.Files.exists(Paths.get("properties/config.properties"))) {
            try {
                Properties.loadConfigPropertiesFromFile();
            } catch (FileNotFoundException e) {
                System.out.println(Output.printColoredLog("[WARNING] PROPERTIES from path: " + path + " not added to system properties"));
                e.printStackTrace();
            }
        }
    }

}
