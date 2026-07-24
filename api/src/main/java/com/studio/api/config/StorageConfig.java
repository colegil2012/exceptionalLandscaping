package com.studio.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * DigitalOcean Spaces speaks S3. Point the standard client at the Spaces
 * endpoint and pass the Spaces key/secret; region is nominal but required.
 */
@Configuration
@Profile("prod")
public class StorageConfig {

    @Bean
    public S3Client spacesClient(
            @Value("${app.storage.spaces.endpoint}") String endpoint,
            @Value("${app.storage.spaces.region}") String region,
            @Value("${app.storage.spaces.access-key}") String accessKey,
            @Value("${app.storage.spaces.secret-key}") String secretKey) {

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
