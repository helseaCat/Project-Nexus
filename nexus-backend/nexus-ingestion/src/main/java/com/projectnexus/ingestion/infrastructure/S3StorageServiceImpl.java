package com.projectnexus.ingestion.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

/**
 * AWS S3 implementation of payload storage.
 * Uploads are best-effort — failures are logged but do not block processing.
 */
@Service
public class S3StorageServiceImpl implements S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageServiceImpl.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageServiceImpl(
            S3Client s3Client,
            @Value("${nexus.storage.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public Optional<String> upload(UUID tenantId, UUID contractId, UUID payloadId, String jsonContent) {
        String key = buildKey(tenantId, contractId, payloadId);
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            byte[] content = jsonContent.getBytes(StandardCharsets.UTF_8);
            s3Client.putObject(request, RequestBody.fromBytes(content));

            log.debug("Uploaded payload to S3: bucket={}, key={}", bucketName, key);
            return Optional.of(key);
        } catch (S3Exception | SdkClientException e) {
            log.error("Failed to upload payload to S3: bucket={}, key={}", bucketName, key, e);
            return Optional.empty();
        }
    }

    /**
     * Generates a deterministic S3 key from the component UUIDs.
     * Pattern: {tenantId}/{contractId}/{payloadId}.json
     */
    static String buildKey(UUID tenantId, UUID contractId, UUID payloadId) {
        return tenantId + "/" + contractId + "/" + payloadId + ".json";
    }
}
