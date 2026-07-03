package com.example.hackathon;

import com.example.hackathon.domain.card.controller.SurvivalCardController;
import com.example.hackathon.domain.image.controller.ImageController;
import com.example.hackathon.domain.user.dto.UserDtos.CreateUserRequest;
import com.example.hackathon.domain.user.dto.UserDtos.UserResponse;
import com.example.hackathon.domain.user.service.UserService;
import com.example.hackathon.global.exception.ErrorResponse;
import com.example.hackathon.global.exception.GlobalExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class HackathonApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
    }

    @Test
    void createUserReturnsCreatedUser() {
        UserResponse response = userService.createUser(new CreateUserRequest("codex07031028"));

        assertThat(response.userId()).isNotNull();
        assertThat(response.nickname()).isEqualTo("codex07031028");
    }

    @Test
    void checkNicknameWithoutNicknameReturnsBadRequest() throws Exception {
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMissingRequestParameter(
                new MissingServletRequestParameterException("nickname", "String")
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
    }

    @Test
    void unexpectedExceptionReturnsInternalServerError() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/folders");
        when(request.getHeader("X-USER-ID")).thenReturn("8");

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnexpected(
                new RuntimeException("test exception"),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
    }

    @Test
    void imageApisAreDisabledAndHiddenFromOpenApi() {
        assertThat(applicationContext.getBeansOfType(ImageController.class)).isEmpty();

        Method updateImageMethod = Arrays.stream(SurvivalCardController.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("updateCardImage"))
                .findFirst()
                .orElseThrow();
        Operation operation = updateImageMethod.getAnnotation(Operation.class);

        assertThat(operation).isNotNull();
        assertThat(operation.hidden()).isTrue();
    }

}
