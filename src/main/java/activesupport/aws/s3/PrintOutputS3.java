package activesupport.aws.s3;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class PrintOutputS3 {
    private static final String BUCKET_SECRET_KEY = "printOutputBucket";
    private static final String PREFIX_SECRET_KEY = "printOutputPrefix";
    private static final String TIMEOUT_SECONDS_SECRET_KEY = "printOutputTimeoutSeconds";
    private static final int DEFAULT_TIMEOUT_SECONDS = 4;
    private static final Pattern PRINT_OUTPUT_FILE_NAME = Pattern.compile("\\d{8}-\\d{6}_job\\d+\\.pdf");
    private static final DateTimeFormatter S3_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private PrintOutputS3() {
    }

    public static PrintOutputFile waitForNonEmptyPdfForQueueId(String queueId) {
        if (queueId == null || queueId.isBlank()) {
            throw new IllegalArgumentException("Queue id must be present before checking print output S3");
        }

        int timeoutSeconds = timeoutSeconds();
        long timeoutAt = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSeconds);

        do {
            Optional<PrintOutputFile> matchingFile = findNonEmptyPdfForQueueId(queueId);
            if (matchingFile.isPresent()) {
                return matchingFile.get();
            }

            sleep();
        } while (System.currentTimeMillis() < timeoutAt);

        throw new AssertionError(String.format(
                "No non-empty print output PDF was created for queue id %s within %s seconds. Recent print output PDFs now: %s",
                queueId, timeoutSeconds, recentFiles()));
    }

    private static Optional<PrintOutputFile> findNonEmptyPdfForQueueId(String queueId) {
        String expectedSuffix = String.format("_job%s.pdf", queueId);

        return listPrintOutputFiles().stream()
                .filter(file -> file.key().endsWith(expectedSuffix))
                .filter(file -> file.size() > 0)
                .max(Comparator.comparing(PrintOutputFile::lastModified));
    }

    private static List<PrintOutputFile> listPrintOutputFiles() {
        String continuationToken = null;
        List<PrintOutputFile> files = new ArrayList<>();

        do {
            ListObjectsV2Request request = new ListObjectsV2Request()
                    .withBucketName(bucket())
                    .withPrefix(datedPrefix())
                    .withMaxKeys(100)
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

    private static String datedPrefix() {
        LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));
        return String.format("%s%s", prefix(), today.format(S3_DATE_FORMAT));
    }

    private static int timeoutSeconds() {
        String timeout = SecretsManager.getSecretValue(TIMEOUT_SECONDS_SECRET_KEY);
        if (timeout == null || timeout.isBlank()) {
            return DEFAULT_TIMEOUT_SECONDS;
        }

        try {
            return Integer.parseInt(timeout.trim());
        } catch (NumberFormatException exception) {
            return DEFAULT_TIMEOUT_SECONDS;
        }
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
