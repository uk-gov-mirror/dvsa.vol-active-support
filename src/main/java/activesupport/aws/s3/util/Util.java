package activesupport.aws.s3.util;

import activesupport.MissingRequiredArgument;
import activesupport.aws.s3.FolderType;
import activesupport.system.Properties;
import activesupport.system.out.Output;
import org.jetbrains.annotations.NotNull;

public class Util {

    public static String s3TempPasswordObjectName(@NotNull String emailAddress) {
        String S3SanitiseObjectName = emailAddress.replaceAll("[@\\.]","");
        return S3SanitiseObjectName + "__Your_temporary_password";
    }

    public static String s3Directory(@NotNull FolderType folderType) throws MissingRequiredArgument {
        return s3Path(folderType);
    }

    public static String s3Path(@NotNull FolderType folderType) throws MissingRequiredArgument {
        return s3Path("", folderType);
    }

    public static String s3Path(@NotNull String S3ObjectName) throws MissingRequiredArgument {
        FolderType folderType = FolderType.EMAIL;
        return s3Path(S3ObjectName, folderType);
    }

    public static String s3Path(@NotNull String S3ObjectName, @NotNull FolderType folderType) throws MissingRequiredArgument {
        String env = Properties.get("env", true);

        if(env.isEmpty()){
            throw new IllegalArgumentException(
                    Output.printColoredLog("[ERROR] SYSTEM PROPERTY: env is not set")
            );
        }

        return String.format("olcs.%s.nonprod.dvsa.aws/%s%s", env, folderType.toString(), S3ObjectName);
    }
}
