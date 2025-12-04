package com.postkar.project3dmodel.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Base64;

@Service
public class AssetService {
    private final S3Client s3Client;
    private final S3Presigner presigner;
    @Value("${cloudflare.r2.bucket}")
    private String bucket;
    @Value("${cloudflare.r2.account-id}")
    private String accountId;
    @Value("${app.signed-url-expiry-minutes}")
    private int expiryMinutes;

    public AssetService(S3Client s3Client, S3Presigner presigner) {
        this.s3Client = s3Client;
        this.presigner = presigner;
    }

    public String uploadFile(MultipartFile file, String key) throws IOException {
        s3Client.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        // Return R2 public URL format
        return String.format("https://%s.%s.r2.cloudflarestorage.com/%s", bucket, accountId, key);
    }

    public String generateSignedUrl(String key) {
        var request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .getObjectRequest(b -> b.bucket(bucket).key(key))
                .build();
        return presigner.presignGetObject(request).url().toString();
    }

    public String computeChecksum(MultipartFile file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(file.getBytes());
        return Base64.getEncoder().encodeToString(md.digest());
    }
}
