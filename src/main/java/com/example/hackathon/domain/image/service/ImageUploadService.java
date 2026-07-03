package com.example.hackathon.domain.image.service;

import com.example.hackathon.global.storage.s3.S3ImageStorageProperties;
import com.example.hackathon.domain.image.dto.ImageDtos.ImageUploadResponse;
import com.example.hackathon.domain.user.service.UserReader;
import com.example.hackathon.global.exception.BadRequestException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class ImageUploadService {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final S3Client s3Client;
    private final S3ImageStorageProperties properties;
    private final UserReader userReader;

    public ImageUploadService(
            S3Client s3Client,
            S3ImageStorageProperties properties,
            UserReader userReader
    ) {
        this.s3Client = s3Client;
        this.properties = properties;
        this.userReader = userReader;
    }

    public ImageUploadResponse uploadCardImage(Long userId, MultipartFile file) {
        userReader.getById(userId);
        validateFile(file);

        String contentType = normalizeContentType(file.getContentType());
        String extension = extensionOf(contentType);
        String imageKey = buildImageKey(userId, extension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(properties.getBucket())
                .key(imageKey)
                .contentType(contentType)
                .contentLength(file.getSize())
                .build();

        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
        } catch (IOException exception) {
            throw new BadRequestException("이미지 파일을 읽을 수 없습니다.");
        }

        return new ImageUploadResponse(buildPublicUrl(imageKey), imageKey);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("업로드할 이미지 파일이 필요합니다.");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new BadRequestException("이미지 파일은 5MB 이하여야 합니다.");
        }
        extensionOf(normalizeContentType(file.getContentType()));
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BadRequestException("이미지 contentType이 필요합니다.");
        }
        return contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
    }

    private String extensionOf(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new BadRequestException("jpeg, png, webp 이미지만 업로드할 수 있습니다.");
        };
    }

    private String buildImageKey(Long userId, String extension) {
        String date = LocalDate.now(SEOUL_ZONE).format(DATE_FORMATTER);
        return "%s/%d/%s/%s.%s".formatted(
                properties.normalizedCardPrefix(),
                userId,
                date,
                UUID.randomUUID(),
                extension
        );
    }

    private String buildPublicUrl(String imageKey) {
        return properties.normalizedPublicBaseUrl() + "/" + imageKey;
    }
}
