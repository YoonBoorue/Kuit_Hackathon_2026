package com.example.hackathon;

import com.example.hackathon.domain.user.dto.UserDtos.CreateUserRequest;
import com.example.hackathon.domain.user.dto.UserDtos.UserResponse;
import com.example.hackathon.domain.user.service.UserService;
import com.example.hackathon.global.exception.ErrorResponse;
import com.example.hackathon.global.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HackathonApplicationTests {

    @Autowired
    private UserService userService;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

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

}
