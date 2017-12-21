package activesupport.aws.s3.util;

import activesupport.system.out.Output;
import org.jetbrains.annotations.NotNull;

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

}
