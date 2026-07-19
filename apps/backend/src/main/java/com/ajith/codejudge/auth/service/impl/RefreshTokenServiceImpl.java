package com.ajith.codejudge.auth.service.impl;

import com.ajith.codejudge.auth.entity.RefreshToken;
import com.ajith.codejudge.auth.repository.RefreshTokenRepository;
import com.ajith.codejudge.auth.service.interfaces.RefreshTokenService;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${app.jwt.refresh-expiration-ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete any existing refresh token for the user to prevent orphan rows
        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.flush();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired() || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new com.ajith.codejudge.exception.UnauthorizedException(
                    "Refresh token was expired or revoked. Please sign in again");
        }
        return token;
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        refreshTokenRepository.deleteByUser(user);
    }
}
