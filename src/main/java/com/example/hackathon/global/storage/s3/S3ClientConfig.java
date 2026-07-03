package com.example.hackathon.global.storage.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {

    @Bean
    public S3ImageStorageProperties s3ImageStorageProperties(
            @Value("${app.image.s3.bucket:summer-survival-mail-images-1}") String bucket,
            @Value("${app.image.s3.region:ap-northeast-2}") String region,
            @Value("${app.image.s3.public-base-url:https://summer-survival-mail-images-1.s3.ap-northeast-2.amazonaws.com}") String publicBaseUrl,
            @Value("${app.image.s3.card-prefix:cards}") String cardPrefix,
            @Value("${app.image.s3.max-size-bytes:5242880}") long maxSizeBytes
    ) {
        return new S3ImageStorageProperties(bucket, region, publicBaseUrl, cardPrefix, maxSizeBytes);
    }

    @Bean
    public S3Client s3Client(S3ImageStorageProperties properties) {
        return S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .build();
    }
}
