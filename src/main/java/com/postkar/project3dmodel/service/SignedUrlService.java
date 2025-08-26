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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;

@Service
public class SignedUrlService {

    private static final Logger logger = LoggerFactory.getLogger(SignedUrlService.class);

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.access.key}")
    private String accessKeyId;

    @Value("${aws.secret.key}")
    private String secretAccessKey;

    @Value("${signed.url.expiry.hours:24}")
    private int expiryHours;

    @Value("${aws.s3.base-url:}")
    private String baseUrl;

    private S3Presigner s3Presigner;

    @PostConstruct
    public void initializePresigner() {
        try {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

            this.s3Presigner = S3Presigner.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                    .build();

            logger.info("S3 Presigner initialized successfully for region: {}", region);
        } catch (Exception e) {
            logger.error("Failed to initialize S3 presigner", e);
            throw new RuntimeException("S3 presigner configuration error", e);
        }
    }

    /**
     * Generate signed URL for S3 object with default expiry
     */
    public String generateSignedUrl(String fileUrl) {
        return generateSignedUrl(fileUrl, expiryHours);
    }

    /**
     * Generate signed URL for S3 object with custom expiry
     */
    public String generateSignedUrl(String fileUrl, int hours) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return null;
        }

        // If it's already a full HTTP URL, check if it needs signing
        if (fileUrl.startsWith("http")) {
            // If using CloudFront, URLs might not need signing
            if (baseUrl != null && !baseUrl.isEmpty() && fileUrl.startsWith(baseUrl)) {
                return fileUrl; // CloudFront URLs typically don't need signing
            }

            // If it's already an S3 URL with parameters, return as is
            if (fileUrl.contains("?")) {
                return fileUrl;
            }
        }

        try {
            String s3Key = extractS3KeyFromUrl(fileUrl);
            if (s3Key == null) {
                logger.warn("Cannot extract S3 key from URL: {}", fileUrl);
                return fileUrl; // Return original URL if we can't parse it
            }

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(hours))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String signedUrl = presignedRequest.url().toString();

            logger.debug("Generated signed URL for key: {} (expires in {} hours)", s3Key, hours);
            return signedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate signed URL for: {}", fileUrl, e);
            return fileUrl; // Return original URL on error
        }
    }

    /**
     * Generate signed URL directly from S3 key
     */
    public String generateSignedUrlFromKey(String s3Key) {
        return generateSignedUrlFromKey(s3Key, expiryHours);
    }

    /**
     * Generate signed URL directly from S3 key with custom expiry
     */
    public String generateSignedUrlFromKey(String s3Key, int hours) {
        if (s3Key == null || s3Key.trim().isEmpty()) {
            return null;
        }

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(hours))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String signedUrl = presignedRequest.url().toString();

            logger.debug("Generated signed URL for key: {} (expires in {} hours)", s3Key, hours);
            return signedUrl;

        } catch (Exception e) {
            logger.error("Failed to generate signed URL for key: {}", s3Key, e);
            return null;
        }
    }

    /**
     * Extract S3 key from various URL formats
     */
    private String extractS3KeyFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle CloudFront URLs
            if (baseUrl != null && !baseUrl.isEmpty() && url.startsWith(baseUrl)) {
                String baseUrlClean = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
                return url.substring(baseUrlClean.length() + 1);
            }

            // Handle S3 direct URLs
            if (url.contains(".amazonaws.com/")) {
                return url.substring(url.indexOf(".amazonaws.com/") + 15);
            }

            // Handle s3:// protocol URLs
            if (url.startsWith("s3://")) {
                String withoutProtocol = url.substring(5);
                if (withoutProtocol.contains("/")) {
                    String bucketAndKey = withoutProtocol.substring(withoutProtocol.indexOf("/") + 1);
                    return bucketAndKey;
                }
            }

            // If no protocol, assume it's already an S3 key
            if (!url.startsWith("http") && !url.startsWith("s3://")) {
                return url;
            }

        } catch (Exception e) {
            logger.error("Error extracting S3 key from URL: {}", url, e);
        }

        return null;
    }

    /**
     * Check if the service is properly configured
     */
    public boolean isConfigured() {
        return s3Presigner != null && bucketName != null && !bucketName.trim().isEmpty();
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (s3Presigner != null) {
            s3Presigner.close();
        }
    }
}