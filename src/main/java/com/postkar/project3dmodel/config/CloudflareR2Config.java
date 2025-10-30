package com.postkar.project3dmodel.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Configuration for Cloudflare R2 Storage.
 * R2 is S3-compatible, so we use the AWS SDK with custom endpoint configuration.
 */
@Configuration
public class CloudflareR2Config {
    
    @Value("${cloudflare.r2.account-id}")
    private String accountId;
    
    @Value("${cloudflare.r2.access-key-id}")
    private String accessKeyId;
    
    @Value("${cloudflare.r2.secret-access-key}")
    private String secretAccessKey;
    
    @Value("${cloudflare.r2.bucket}")
    private String bucketName;
    
    @Value("${cloudflare.r2.endpoint:}")
    private String customEndpoint;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        // Cloudflare R2 endpoint format: https://<account-id>.r2.cloudflarestorage.com
        String endpoint = customEndpoint != null && !customEndpoint.isEmpty() 
            ? customEndpoint 
            : String.format("https://%s.r2.cloudflarestorage.com", accountId);
        
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto")) // R2 uses "auto" region
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(false) // R2 uses virtual-hosted-style
                        .build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        
        String endpoint = customEndpoint != null && !customEndpoint.isEmpty() 
            ? customEndpoint 
            : String.format("https://%s.r2.cloudflarestorage.com", accountId);
        
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of("auto"))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(false)
                        .build())
                .build();
    }
}
