package activesupport.system;

import activesupport.system.out.Output;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Properties {
    private static final String defaultConfigPropertiesPath = "properties/config.properties";

    public static void set(@NotNull String property, @NotNull String value){
        String message = String.format("[INFO] SET PROPERTY: %s=%s", property, value);
        System.out.println(Output.printColoredLog(message));
    }

    /**
     * Adds to system properties the properties specified at properties/config.properties
     * */
    public static void loadConfigPropertiesFromFile() throws FileNotFoundException{
        loadProperties(defaultConfigPropertiesPath);
    }

    /**
     * Adds the properties of the property file at the specified path to system properties.
     * @param path path to a properties file from project root.
     * */
    public static void loadProperties(@NotNull String path) throws FileNotFoundException{
        if(!Files.exists(Paths.get(path))){
            throw new FileNotFoundException(Output.printColoredLog(String.format("[ERROR] %s does not exist", path.toString())));
        }

        java.util.Properties properties = new java.util.Properties();

        try(InputStream inputStream = new FileInputStream(path)){
            properties.putAll(System.getProperties());
            properties.load(inputStream);
            inputStream.close();
            System.setProperties(properties);
        } catch (IOException e){
            Output.printColoredLog(String.format("[WARNING] Unable to find or load system properties at %s", path));
        }
    }

    public static void writeToConfigPropertyFile(@NotNull String key, @NotNull String value) throws IOException {
        writeProperty(key, value, defaultConfigPropertiesPath);
    }

    public static void writeToConfigPropertyFile(@NotNull HashMap<String, String> userProperties) throws IOException {
        writeProperties(userProperties, defaultConfigPropertiesPath);
    }

    public static void writeProperty(@NotNull String key, @NotNull String value, @NotNull String path) throws IOException {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(key, value);

        writeProperties(properties, path);
    }

    public static void writeProperties(@NotNull HashMap<String, String> userProperties, @NotNull String path) throws IOException {
        java.util.Properties properties = new java.util.Properties();

        (new File(path)).getParentFile().mkdirs();
        try(OutputStream outputStream = new FileOutputStream(path, true)) {

            for (Map.Entry<String, String> entry : userProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                properties.setProperty(key, value);

                Output.printColoredLog(String.format("[INFO] PROPERTY  SET: %s=%s", key, value));
            }

            properties.store(outputStream, null);
        }

        Output.printColoredLog(String.format("[INFO] PROPERTIES STORED: Path to file is %s", path));
    }

}
