package activesupport.system;

import activesupport.system.out.Output;
import org.jetbrains.annotations.NotNull;
import sun.security.krb5.internal.PAData;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Properties {

    public static void loadDefaultProperties() throws FileNotFoundException{
        String defaultPropsPath = "properties/default.properties";
        loadProperties(defaultPropsPath);
    }

    public static void loadProperties(@NotNull String path) throws FileNotFoundException{
        if(!Files.exists(Paths.get(path))){
            throw new FileNotFoundException(Output.prettyPrint(String.format("[ERROR] %s does not exist", path.toString())));
        }

        java.util.Properties properties = new java.util.Properties();

        try(InputStream inputStream = new FileInputStream(path)){
            properties.putAll(System.getProperties());
            properties.load(inputStream);
            inputStream.close();
            System.setProperties(properties);
        } catch (IOException e){
            Output.prettyPrint(String.format("[ERROR] Unable to find or load system properties at %s", path));
        }
    }

    public static void writeProperty(@NotNull String key, @NotNull String value, @NotNull String path) throws IOException {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put(key, value);

        writeProperties(properties, path);
    }

    public static void writeProperties(@NotNull HashMap<String, String> userProperties, @NotNull String path) throws IOException {
        java.util.Properties properties = new java.util.Properties();

        (new File(path)).getParentFile().mkdirs();
        try(OutputStream outputStream = new FileOutputStream(path)) {

            for (Map.Entry<String, String> entry : userProperties.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                properties.setProperty(key, value);

                Output.prettyPrint(String.format("[INFO] PROPERTY  SET: %s=%s", key, value));
            }

            properties.store(outputStream, null);
        }

        Output.prettyPrint(String.format("[INFO] PROPERTIES STORED: Path to file is %s", path));
    }

}
