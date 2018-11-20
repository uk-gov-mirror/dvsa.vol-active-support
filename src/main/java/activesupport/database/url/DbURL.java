package activesupport.database.url;

public class DbURL {

    public String getDBUrl(String env) {
        String dbURL;
        switch (env.toLowerCase().trim()) {
            case "int":
            case "pp":
                dbURL = String.format("https://ssap1.olcs.%s.prod.dvsa.aws/",env);
                break;
            default:
                dbURL = String.format("https://ssap1.olcs.%s.nonprod.dvsa.aws/",env);
                break;
        }
        return dbURL.toLowerCase().trim();
    }
}