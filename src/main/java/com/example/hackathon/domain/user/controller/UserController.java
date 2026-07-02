package com.example.hackathon.domain.user.controller;

import com.example.hackathon.domain.user.controller.dto.UserDtos.CreateUserRequest;
import com.example.hackathon.domain.user.controller.dto.UserDtos.NicknameCheckResponse;
import com.example.hackathon.domain.user.controller.dto.UserDtos.UserHomeResponse;
import com.example.hackathon.domain.user.controller.dto.UserDtos.UserResponse;
import com.example.hackathon.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "사용자 닉네임과 기본 정보를 관리하는 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "사용자 생성", description = "로그인 없이 사용할 임시 사용자를 닉네임 기반으로 생성합니다.")
    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임 입력 화면에서 시작하기 버튼 활성화 여부를 판단하기 위해 닉네임 사용 가능 여부를 확인합니다.")
    @GetMapping("/nickname/check")
    public NicknameCheckResponse checkNickname(
            @Parameter(description = "확인할 닉네임", required = true)
            @RequestParam String nickname
    ) {
        return userService.checkNickname(nickname);
    }

    @Operation(summary = "내 정보 조회", description = "X-USER-ID 헤더에 해당하는 사용자의 기본 정보를 조회합니다.")
    @GetMapping("/me")
    public UserResponse getMe(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return userService.getMe(userId);
    }

    @Operation(summary = "홈 정보 조회", description = "내가 만든 카드, 받은 카드, 발송 가능한 카드 개수를 조회합니다.")
    @GetMapping("/me/home")
    public UserHomeResponse getHome(
            @Parameter(description = "프론트에서 보관하는 임시 사용자 ID", required = true)
            @RequestHeader("X-USER-ID") Long userId
    ) {
        return userService.getHome(userId);
    }
}
