package com.example.hackathon.domain.image.config;

public class S3ImageStorageProperties {

    private final String bucket;
    private final String region;
    private final String publicBaseUrl;
    private final String cardPrefix;
    private final long maxSizeBytes;

    public S3ImageStorageProperties(
            String bucket,
            String region,
            String publicBaseUrl,
            String cardPrefix,
            long maxSizeBytes
    ) {
        this.bucket = requireText(bucket, "S3 bucket 설정이 필요합니다.");
        this.region = requireText(region, "S3 region 설정이 필요합니다.");
        this.publicBaseUrl = requireText(publicBaseUrl, "S3 publicBaseUrl 설정이 필요합니다.");
        this.cardPrefix = requireText(cardPrefix, "S3 cardPrefix 설정이 필요합니다.");
        if (maxSizeBytes <= 0) {
            throw new IllegalArgumentException("이미지 최대 크기 설정은 0보다 커야 합니다.");
        }
        this.maxSizeBytes = maxSizeBytes;
    }

    public String getBucket() {
        return bucket;
    }

    public String getRegion() {
        return region;
    }

    public String getPublicBaseUrl() {
        return publicBaseUrl;
    }


    public String getCardPrefix() {
        return cardPrefix;
    }


    public long getMaxSizeBytes() {
        return maxSizeBytes;
    }

    public String normalizedPublicBaseUrl() {
        return stripTrailingSlash(publicBaseUrl.trim());
    }

    public String normalizedCardPrefix() {
        return stripSlashes(cardPrefix.trim());
    }

    private String stripTrailingSlash(String value) {
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String stripSlashes(String value) {
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return stripTrailingSlash(value);
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
