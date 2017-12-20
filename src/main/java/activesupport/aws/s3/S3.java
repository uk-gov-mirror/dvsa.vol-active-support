package activesupport.aws.s3;

import activesupport.aws.s3.util.Util;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.jetbrains.annotations.NotNull;

import java.util.Scanner;

public class S3 {
    /**
     * This extracts the temporary password out out the emails stored in the S3 bucket.
     * The specific object that the password will be extracted out of if inferred from the emailAddress.
     * @param emailAddress This is the email address used to create an account on external(self-serve).
     * @param S3BucketName This is the name of the S3 bucket.
     * */
    public static String getTempPassword(@NotNull String emailAddress, @NotNull String S3BucketName){
        String S3ObjectName = Util.s3TempPasswordObjectName(emailAddress);
        String S3Path = Util.s3Path(S3ObjectName);
        S3Object s3Object = S3.getS3Object(S3BucketName, S3Path);
        return extractTempPasswordFromS3Object(s3Object);
    }

    private static String extractTempPasswordFromS3Object(S3Object s3Object) {
        Scanner scanner = new Scanner(s3Object.getObjectContent());
        return scanner.findWithinHorizon("/[\\w]{11}(?==0A\\n)/", 0);
    }

    private static S3Object getS3Object(String s3BucketName, String s3Path) {
        return createS3Client().getObject(new GetObjectRequest(s3BucketName, s3Path));
    }

    private static AmazonS3 createS3Client(){
        boolean refreshCredentialsAsync = false;
        return createS3Client(refreshCredentialsAsync);
    }

    private static AmazonS3 createS3Client(boolean refreshCredentialsAsync){
        String region = "eu-west-1";
        AmazonS3 s3;

        if(S3.hasAWSAccessKeyIDENV() && S3.hasAWSSecretAccessKeyENV()){
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

    private static boolean hasAWSAccessKeyIDENV(){
        return !getAWSAccessKeyID().isEmpty();
    }

    private static boolean hasAWSSecretAccessKeyENV(){
        return !getAWSSecretAccessKey().isEmpty();
    }

    private static String getAWSAccessKeyID() {
        return System.getProperty("AWS_ACCESS_KEY_ID");
    }

    private static String getAWSSecretAccessKey() {
        return System.getProperty("AWS_SECRET_ACCESS_KEY");
    }
}
