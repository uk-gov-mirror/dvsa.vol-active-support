package activesupport.aws.s3;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PrintOutputS3 {
    private static final String BUCKET_SECRET_KEY = "printOutputBucket";
    private static final String PREFIX_SECRET_KEY = "printOutputPrefix";
    private static final String TIMEOUT_SECONDS_SECRET_KEY = "printOutputTimeoutSeconds";
    private static final String DEFAULT_TIMEOUT_SECONDS = "15";
    private static final Pattern PRINT_OUTPUT_FILE_NAME = Pattern.compile("\\d{8}-\\d{6}_job\\d+\\.pdf");

    private PrintOutputS3() {
    }

    public static Set<String> currentPdfKeys() {
        return listPrintOutputFiles().stream()
                .map(PrintOutputFile::key)
                .collect(Collectors.toSet());
    }

    public static PrintOutputFile waitForNewNonEmptyPdf(Set<String> existingKeys, String queueId) {
        long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds());

        do {
            Optional<PrintOutputFile> matchingFile = findNewNonEmptyPdf(existingKeys, queueId);
            if (matchingFile.isPresent()) {
                return matchingFile.get();
            }

            sleep();
        } while (System.currentTimeMillis() < timeoutAt);

        throw new AssertionError(String.format(
                "No new non-empty print output PDF was created in s3://%s/%s within %s seconds. Queue id: %s. Existing PDF count before print: %s. Recent PDFs now: %s",
                bucket(), prefix(), timeoutSeconds(), queueId, existingKeys.size(), recentFiles()));
    }

    private static Optional<PrintOutputFile> findNewNonEmptyPdf(Set<String> existingKeys, String queueId) {
        List<PrintOutputFile> files = listPrintOutputFiles();

        if (queueId != null && !queueId.isBlank()) {
            String expectedSuffix = String.format("_job%s.pdf", queueId);
            Optional<PrintOutputFile> queueFile = files.stream()
                    .filter(file -> !existingKeys.contains(file.key()))
                    .filter(file -> file.key().endsWith(expectedSuffix))
                    .filter(file -> file.size() > 0)
                    .findFirst();

            if (queueFile.isPresent()) {
                return queueFile;
            }
        }

        return files.stream()
                .filter(file -> !existingKeys.contains(file.key()))
                .filter(file -> file.size() > 0)
                .max(Comparator.comparing(PrintOutputFile::lastModified));
    }

    private static List<PrintOutputFile> listPrintOutputFiles() {
        String continuationToken = null;
        List<PrintOutputFile> files = new ArrayList<>();

        do {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucket())
                    .withPrefix(prefix())
                    .withContinuationToken(continuationToken);

            ListObjectsV2Result result = S3.client(Regions.EU_WEST_1).listObjectsV2(request);
            result.getObjectSummaries().stream()
                    .filter(summary -> PRINT_OUTPUT_FILE_NAME.matcher(fileName(summary)).matches())
                    .map(PrintOutputS3::toPrintOutputFile)
                    .forEach(files::add);

            continuationToken = result.getNextContinuationToken();
        } while (continuationToken != null);

        return files;
    }

    private static String bucket() {
        return requiredSecret(BUCKET_SECRET_KEY);
    }

    private static String prefix() {
        return requiredSecret(PREFIX_SECRET_KEY);
    }

    private static int timeoutSeconds() {
        String timeout = SecretsManager.getSecretValue(TIMEOUT_SECONDS_SECRET_KEY);
        return Integer.parseInt(timeout == null || timeout.isBlank() ? DEFAULT_TIMEOUT_SECONDS : timeout);
    }

    private static String requiredSecret(String secretKey) {
        String value = SecretsManager.getSecretValue(secretKey);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(String.format("Secret value '%s' must be configured", secretKey));
        }
        return value;
    }

    private static String recentFiles() {
        List<PrintOutputFile> files = listPrintOutputFiles();
        if (files.isEmpty()) {
            return "none";
        }

        return files.stream()
                .sorted(Comparator.comparing(PrintOutputFile::lastModified).reversed())
                .limit(5)
                .map(file -> String.format("%s (%s bytes)", file.name(), file.size()))
                .collect(Collectors.joining(", "));
    }

    private static String fileName(S3ObjectSummary summary) {
        String key = summary.getKey();
        return key.substring(key.lastIndexOf('/') + 1);
    }

    private static PrintOutputFile toPrintOutputFile(S3ObjectSummary summary) {
        return new PrintOutputFile(
                summary.getKey(),
                fileName(summary),
                summary.getSize(),
                summary.getLastModified().toInstant());
    }

    private static void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(500);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for print output PDF", exception);
        }
    }

    public record PrintOutputFile(String key, String name, long size, Instant lastModified) {
    }
}
