package com.postkar.project3dmodel.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class SignedUrlService {

    private static final Logger logger = LoggerFactory.getLogger(SignedUrlService.class);

    @Value("${cloudflare.r2.bucket}")
    private String bucketName;

    @Value("${cloudflare.r2.account-id}")
    private String accountId;

    @Value("${cloudflare.r2.access-key-id}")
    private String accessKeyId;

    @Value("${cloudflare.r2.secret-access-key}")
    private String secretAccessKey;

    @Value("${cloudflare.r2.endpoint:}")
    private String customEndpoint;

    @Value("${signed.url.expiry.hours:24}")
    private int expiryHours;

    @Value("${cloudflare.r2.public-url:}")
    private String publicUrl;

    private S3Presigner s3Presigner;

    @PostConstruct
    public void initializePresigner() {
        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            // Cloudflare R2 endpoint format: https://<account-id>.r2.cloudflarestorage.com
            String endpoint = customEndpoint != null && !customEndpoint.isEmpty() 
                ? customEndpoint 
                : String.format("https://%s.r2.cloudflarestorage.com", accountId);

            this.s3Presigner = S3Presigner.builder()
                    .endpointOverride(java.net.URI.create(endpoint))
                    .region(Region.of("auto")) // R2 uses "auto" region
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .serviceConfiguration(S3Configuration.builder()
                            .pathStyleAccessEnabled(false) // R2 uses virtual-hosted-style
                            .build())
                    .build();

            logger.info("Cloudflare R2 Presigner initialized successfully for account: {}", accountId);
        } catch (Exception e) {
            logger.error("Failed to initialize R2 presigner", e);
            throw new RuntimeException("R2 presigner configuration error", e);
        }
    }


    public String generateSignedUrl(String fileUrl) {
        return generateSignedUrl(fileUrl, expiryHours);
    }


    public String generateSignedUrl(String fileUrl, int hours) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }

        if (fileUrl.startsWith("http")) {
            if (publicUrl != null && !publicUrl.isEmpty() && fileUrl.startsWith(publicUrl)) {
                return fileUrl; // Custom public domain URLs typically don't need signing
            }

            if (fileUrl.contains("?")) {
                return fileUrl;
            }
        }

        try {
            String r2Key = extractS3KeyFromUrl(fileUrl);
            if (r2Key == null) {
                logger.warn("Cannot extract R2 key from URL: {}", fileUrl);
                return fileUrl;
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(r2Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(hours))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String signedUrl = presignedRequest.url().toString();

            logger.debug("Generated signed URL for R2 key: {} (expires in {} hours)", r2Key, hours);
            return signedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate signed URL for: {}", fileUrl, e);
            return fileUrl;
        }
    }


    public String generateSignedUrlFromKey(String r2Key) {
        return generateSignedUrlFromKey(r2Key, expiryHours);
    }

    public String generateSignedUrlFromKey(String r2Key, int hours) {
        if (r2Key == null || r2Key.trim().isEmpty()) {
            return null;
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(r2Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(hours))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String signedUrl = presignedRequest.url().toString();

            logger.debug("Generated signed URL for R2 key: {} (expires in {} hours)", r2Key, hours);
            return signedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate signed URL for R2 key: {}", r2Key, e);
            return null;
        }
    }


    private String extractS3KeyFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle custom public URL
            if (publicUrl != null && !publicUrl.isEmpty() && url.startsWith(publicUrl)) {
                String baseUrlClean = publicUrl.endsWith("/") ? publicUrl.substring(0, publicUrl.length() - 1) : publicUrl;
                return url.substring(baseUrlClean.length() + 1);
            }

            // Handle R2 cloudflarestorage.com URLs
            if (url.contains(".r2.cloudflarestorage.com/")) {
                return url.substring(url.indexOf(".r2.cloudflarestorage.com/") + 26);
            }

            // Legacy S3 URL support (for migration)
            if (url.contains(".amazonaws.com/")) {
                return url.substring(url.indexOf(".amazonaws.com/") + 15);
            }

            // Handle s3:// or r2:// protocol
            if (url.startsWith("s3://") || url.startsWith("r2://")) {
                String withoutProtocol = url.substring(5);
                if (withoutProtocol.contains("/")) {
                    String bucketAndKey = withoutProtocol.substring(withoutProtocol.indexOf("/") + 1);
                    return bucketAndKey;
                }
            }

            // If it's just a key without protocol or domain
            if (!url.startsWith("http") && !url.startsWith("s3://") && !url.startsWith("r2://")) {
                return url;
            }

        } catch (Exception e) {
            logger.error("Error extracting R2 key from URL: {}", url, e);
        }

        return null;
    }


    public boolean isConfigured() {
        return s3Presigner != null && bucketName != null && !bucketName.trim().isEmpty();
    }

    public void cleanup() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}