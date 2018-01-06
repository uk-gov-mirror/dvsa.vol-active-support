package activesupport.url;

import activesupport.string.Str;
import org.jetbrains.annotations.NotNull;

public class URL {

    public static String extractScheme(@NotNull String URL){
        String regex = "https?";
        return Str.find(regex, URL);
    }

    public static String extractDomain(@NotNull String URL){
        String regex = "(?<=:\\/\\/)[A-z\\.]+\\/?";
        String domain = Str.find(regex, URL);
        return domain.endsWith("/") ? domain : domain + "/";
    }

    public static String extractPath(@NotNull String URL){
        String regex = "(?<=[A-z]\\/)[\\w\\/]+";
        String path = Str.find(regex, URL);
        return path.endsWith("/") ? path : path + "/";
    }

}
