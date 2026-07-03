package com.example.hackathon.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "KUIT Hackathon 2026 API",
                description = "KUIT Hackathon 2026 서버 API 문서",
                version = "v1",
                contact = @Contact(name = "KUIT Hackathon Team")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer globalErrorResponseCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            openApi.getComponents().addSchemas("ErrorResponse", errorResponseSchema());

            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations()
                    .forEach(operation -> {
                        ApiResponses responses = operation.getResponses();
                        addErrorResponse(responses, "400", "잘못된 요청");
                        addErrorResponse(responses, "403", "접근 권한 없음");
                        addErrorResponse(responses, "404", "존재하지 않는 리소스");
                        addErrorResponse(responses, "409", "상태 충돌 또는 중복 요청");
                        addErrorResponse(responses, "503", "외부 서비스 사용 불가");
                        addErrorResponse(responses, "500", "서버 내부 오류");
                    }));
        };
    }

    private Schema<?> errorResponseSchema() {
        return new ObjectSchema()
                .addProperty("status", new IntegerSchema().example(400))
                .addProperty("message", new StringSchema().example("요청 값이 올바르지 않습니다."))
                .addProperty("timestamp", new StringSchema().format("date-time"));
    }

    private void addErrorResponse(ApiResponses responses, String status, String description) {
        if (responses.containsKey(status)) {
            return;
        }
        responses.addApiResponse(status, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                )));
    }
}
