package com.example.hackathon.domain.image.controller;

import com.example.hackathon.domain.image.dto.ImageDtos.ImageUploadResponse;
import com.example.hackathon.domain.image.service.ImageUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이미지", description = "카드 이미지 파일을 백엔드 multipart 방식으로 S3에 업로드하는 API")
@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageUploadService imageUploadService;

    public ImageController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @Operation(summary = "카드 이미지 업로드", description = "이미지 파일을 multipart/form-data로 받아 S3에 업로드하고 imageUrl과 imageKey를 반환합니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse uploadImage(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId,
            @Parameter(description = "업로드할 이미지 파일. jpeg, png, webp만 가능하며 최대 5MB입니다.", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        return imageUploadService.uploadCardImage(userId, file);
    }
}
