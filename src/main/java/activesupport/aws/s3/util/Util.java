package activesupport.aws.s3.util;

import activesupport.aws.s3.FolderType;
import activesupport.system.out.Output;
import org.jetbrains.annotations.NotNull;

public class Util {

    public static String s3TempPasswordObjectName(@NotNull String emailAddress) {
        String S3SanitiseObjectName = emailAddress.replaceAll("[@\\.]","");
        return S3SanitiseObjectName + "__Your_temporary_password";
    }

    public static String s3Path(@NotNull String S3ObjectName) throws IllegalAccessException {
        FolderType folderType = FolderType.EMAIL;
        return s3Path(S3ObjectName, folderType);
    }

    public static String s3Directory(@NotNull FolderType folderType) throws IllegalAccessException {
        String env = System.getProperty("env");

        if(env.isEmpty()){
            throw new IllegalArgumentException(
                    Output.printColoredLog("[ERROR] SYSTEM PROPERTY: env is not set")
            );
        }

        return String.format("olcs.%s.nonprod.dvsa.aws/%s/", env, folderTypeName(folderType));
    }

    public static String s3Path(@NotNull String S3ObjectName, @NotNull FolderType folderType) throws IllegalAccessException {
        return s3Directory(folderType) + S3ObjectName;
    }

    private static String folderTypeName(@NotNull FolderType folderType) throws IllegalAccessException {
        String folderTypeName;

        switch (folderType) {
            case EMAIL:
                folderTypeName = "email";
                break;
            case NI_EXPORT:
                folderTypeName = "data-gov-ni-export";
                break;
            default:
                throw new IllegalAccessException(Output.printColoredLog("[ERROE] Unsupported folder type: " + folderType));
        }

        return folderTypeName;
    }

}
