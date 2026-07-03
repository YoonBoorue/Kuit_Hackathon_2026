package com.example.hackathon.domain.user.service;

import com.example.hackathon.domain.card.entity.CardStatus;
import com.example.hackathon.domain.card.repository.SurvivalCardRepository;
import com.example.hackathon.domain.collection.entity.CollectionSource;
import com.example.hackathon.domain.collection.repository.CollectionCardRepository;
import com.example.hackathon.domain.user.dto.UserDtos.CreateUserRequest;
import com.example.hackathon.domain.user.dto.UserDtos.NicknameCheckResponse;
import com.example.hackathon.domain.user.dto.UserDtos.UserHomeResponse;
import com.example.hackathon.domain.user.dto.UserDtos.UserResponse;
import com.example.hackathon.domain.user.entity.User;
import com.example.hackathon.domain.user.repository.UserRepository;
import com.example.hackathon.global.exception.BadRequestException;
import com.example.hackathon.global.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserReader userReader;
    private final CollectionCardRepository collectionCardRepository;
    private final SurvivalCardRepository survivalCardRepository;

    public UserService(
            UserRepository userRepository,
            UserReader userReader,
            CollectionCardRepository collectionCardRepository,
            SurvivalCardRepository survivalCardRepository
    ) {
        this.userRepository = userRepository;
        this.userReader = userReader;
        this.collectionCardRepository = collectionCardRepository;
        this.survivalCardRepository = survivalCardRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        String nickname = normalizeNickname(request.nickname());
        if (userRepository.existsByNickname(nickname)) {
            throw new ConflictException("이미 사용 중인 닉네임입니다.");
        }
        User user = userRepository.save(new User(nickname));
        return toUserResponse(user);
    }

    public NicknameCheckResponse checkNickname(String nickname) {
        String normalizedNickname = normalizeNickname(nickname);
        boolean available = !userRepository.existsByNickname(normalizedNickname);
        String message = available ? "사용 가능한 닉네임입니다." : "이미 사용 중인 닉네임입니다.";
        return new NicknameCheckResponse(normalizedNickname, available, message);
    }

    public UserResponse getMe(Long userId) {
        User user = userReader.getById(userId);
        return toUserResponse(user);
    }

    public UserHomeResponse getHome(Long userId) {
        User user = userReader.getById(userId);
        long createdCardCount = collectionCardRepository.countByUser_IdAndSource(userId, CollectionSource.CREATED);
        long receivedCardCount = collectionCardRepository.countByUser_IdAndSource(userId, CollectionSource.RECEIVED);
        long sendableCardCount = survivalCardRepository.countByAuthorUser_IdAndStatus(userId, CardStatus.UNSENT);

        return new UserHomeResponse(
                user.getNickname(),
                createdCardCount,
                receivedCardCount,
                sendableCardCount
        );
    }

    private String normalizeNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            throw new BadRequestException("닉네임은 필수입니다.");
        }
        return nickname.trim();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getNickname());
    }
}
