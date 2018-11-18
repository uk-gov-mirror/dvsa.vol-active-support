package activesupport.system;

import activesupport.MissingRequiredArgument;
import activesupport.system.out.Output;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Properties {

    public static void set(@NotNull String property, @NotNull String value){
        value = hidePasswords(property, value);
        String message = String.format("\n[INFO] PROPERTY SET: %s=%s\n", property, value);
        Output.printColoredLog(message);
        System.setProperty(property, value);
    }

    public static String get(@NotNull String property) {
        boolean required = false;
        return get(property, required);
    }

    public static String get(@NotNull String property, boolean required) {
        String propValue = null;

        if (System.getenv(property) != null) {
            propValue = System.getenv(property);
        } else if (System.getProperty(property) != null) {
            propValue = System.getProperty(property);
        }

        if (required && propValue == null) {
            throw new MissingRequiredArgument("[ERROR] " + property + " is required and must be set either at runtime or as a system variable");
        }

        propValue = hidePasswords(property, propValue);

        String message = String.format("\n[INFO] PROPERTY RETRIEVED: %s=%s\n", property, propValue);
        Output.printColoredLog(message);
        return propValue;
    }

    public static boolean has(@NotNull String property) {
        String propValue = Properties.get(property);
        return propValue != null && !propValue.isEmpty();
    }

    private static String hidePasswords(@NotNull String property, @NotNull String value) {
        return StringUtils.containsIgnoreCase(property, "password") && !value.isEmpty() ? value.replaceAll("\\w", "*") : value ;
    }

}
