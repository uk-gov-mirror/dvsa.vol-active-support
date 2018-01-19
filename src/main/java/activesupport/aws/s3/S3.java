package activesupport.aws.s3;

import activesupport.aws.s3.util.Util;
import activesupport.string.Str;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class S3 {
    private static AmazonS3 s3;
    private static String s3BucketName = "devapp-olcs-pri-olcs-autotest-s3";

    public static String getNIGVExport(@NotNull String S3ObjectName) throws IllegalAccessException {
        String S3Path = Util.s3Path(S3ObjectName, FolderType.NI_EXPORT);
        S3Object s3Object = S3.getS3Object(s3BucketName, S3Path);
        return objectContents(s3Object);
    }

    public static String objectContents(@NotNull S3Object s3Object) {
        return Str.inputStreamContents(s3Object.getObjectContent());
    }

    public static List<String> getlistFiles(String s3BucketName, String s3Path) {
        s3 = createS3Client();
        ObjectListing objects = s3.listObjects(s3BucketName, s3Path);
        do {
            for (S3ObjectSummary objectSummary : objects.getObjectSummaries()) {
                System.out.println(objectSummary.getKey() + "\t" +
                        objectSummary.getSize() + "\t" +
                        StringUtils.fromDate(objectSummary.getLastModified()));
            }
            objects = s3.listNextBatchOfObjects(objects);
        } while (objects.isTruncated());
        return (List<String>) objects;
    }

    /**
     * This extracts the temporary password out out the emails stored in the S3 bucket.
     * The specific object that the password will be extracted out of if inferred from the emailAddress.
     *
     * @param emailAddress This is the email address used to create an account on external(self-serve).
     * @param S3BucketName This is the name of the S3 bucket.
     */
    public static String getTempPassword(@NotNull String emailAddress, @NotNull String S3BucketName) throws IllegalAccessException {
        String S3ObjectName = Util.s3TempPasswordObjectName(emailAddress);
        String S3Path = Util.s3Path(S3ObjectName);
        S3Object s3Object = S3.getS3Object(S3BucketName, S3Path);
        return extractTempPasswordFromS3Object(s3Object);
    }

    /**
     * This extracts the temporary password out out the emails stored in the S3 bucket.
     * The specific object that the password will be extracted out of if inferred from the emailAddress.
     *
     * @param emailAddress This is the email address used to create an account on external(self-serve).
     */
    public static String getTempPassword(@NotNull String emailAddress) throws IllegalAccessException {
        return getTempPassword(emailAddress, s3BucketName);
    }

    private static String extractTempPasswordFromS3Object(S3Object s3Object) {
        String s3ObjContents = new Scanner(s3Object.getObjectContent()).useDelimiter("\\A").next();
        Pattern pattern = Pattern.compile("[\\w]{6,20}(?==0ASign in at)");
        Matcher matcher = pattern.matcher(s3ObjContents);
        matcher.find();
        String tempPassword = matcher.group();
        return tempPassword;

    }

    private static S3Object getS3Object(String s3BucketName, String s3Path) {
        return createS3Client().getObject(new GetObjectRequest(s3BucketName, s3Path));
    }

    private static AmazonS3 createS3Client() {
        boolean refreshCredentialsAsync = false;
        return createS3Client(refreshCredentialsAsync);
    }

    private static AmazonS3 createS3Client(boolean refreshCredentialsAsync) {
        String region = "eu-west-1";

        if (S3.hasAWSAccessKeyIDENV() && S3.hasAWSSecretAccessKeyENV()) {
            s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(getAWSAccessKeyID(), getAWSSecretAccessKey())))
                    .withRegion(region)
                    .build();
        } else {
            s3 = AmazonS3ClientBuilder.standard()
                    .withCredentials(new InstanceProfileCredentialsProvider(refreshCredentialsAsync))
                    .withRegion(region).build();
        }

        return s3;
    }

    private static boolean hasAWSAccessKeyIDENV() {
        return getAWSAccessKeyID() != null;
    }

    private static boolean hasAWSSecretAccessKeyENV() {
        return getAWSSecretAccessKey() != null;
    }

    private static String getAWSAccessKeyID() {
        return System.getenv("AWS_ACCESS_KEY_ID");
    }

    private static String getAWSSecretAccessKey() {
        return System.getenv("AWS_SECRET_ACCESS_KEY");
    }
}
