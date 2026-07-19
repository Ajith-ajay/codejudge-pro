package com.ajith.codejudge.auth.service.impl;

import com.ajith.codejudge.auth.dto.request.LoginRequest;
import com.ajith.codejudge.auth.dto.request.RegisterRequest;
import com.ajith.codejudge.auth.dto.request.ResetPasswordRequest;
import com.ajith.codejudge.auth.dto.request.TokenRefreshRequest;
import com.ajith.codejudge.auth.dto.request.UpdatePasswordRequest;
import com.ajith.codejudge.auth.dto.response.JwtResponse;
import com.ajith.codejudge.auth.dto.response.TokenRefreshResponse;
import com.ajith.codejudge.auth.entity.RefreshToken;
import com.ajith.codejudge.auth.service.interfaces.AuthService;
import com.ajith.codejudge.auth.service.interfaces.RefreshTokenService;
import com.ajith.codejudge.exception.BadRequestException;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.exception.ResourceNotFoundException;
import com.ajith.codejudge.exception.UnauthorizedException;
import com.ajith.codejudge.notification.service.interfaces.EmailService;
import com.ajith.codejudge.security.jwt.JwtProvider;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import com.ajith.codejudge.user.entity.Role;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.RoleRepository;
import com.ajith.codejudge.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new ConflictException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        Role candidateRole = roleRepository.findByName("ROLE_CANDIDATE")
                .orElseThrow(() -> new ResourceNotFoundException("Default Candidate Role not found"));

        String verificationToken = UUID.randomUUID().toString();

        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .enabled(false) // Disabled until email verified
                .emailVerified(false)
                .verificationToken(verificationToken)
                .roles(Collections.singleton(candidateRole))
                .build();

        userRepository.save(user);
        log.info("Registered user: {}. Sending verification email...", user.getUsername());
        
        emailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Override
    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsernameOrEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtProvider.generateJwtToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userPrincipal.getId());

        List<String> roles = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        log.info("Successfully authenticated user: {}", userPrincipal.getUsername());

        return JwtResponse.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest) {
        String requestRefreshToken = refreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtProvider.generateTokenFromUsername(user.getUsername());
                    // Rotate the refresh token for additional security
                    RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getId());
                    log.info("Rotated refresh token for user: {}", user.getUsername());
                    return TokenRefreshResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(newRefreshToken.getToken())
                            .build();
                })
                .orElseThrow(() -> new UnauthorizedException("Refresh token is not present in database"));
    }

    @Override
    @Transactional
    public void logoutUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userPrincipal) {
            log.info("Logging out user: {}", userPrincipal.getUsername());
            refreshTokenService.deleteByUserId(userPrincipal.getId());
        }
        SecurityContextHolder.clearContext();
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        log.info("User {} successfully verified email", user.getUsername());
    }

    @Override
    @Transactional
    public void requestPasswordReset(ResetPasswordRequest resetRequest) {
        User user = userRepository.findByEmail(resetRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + resetRequest.getEmail()));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // 1 hour expiry
        userRepository.save(user);

        log.info("Requested password reset for {}. Sending email...", user.getUsername());
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Override
    @Transactional
    public void updatePassword(UpdatePasswordRequest updateRequest) {
        User user = userRepository.findByResetToken(updateRequest.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
            throw new BadRequestException("Password reset token has expired");
        }

        user.setPassword(passwordEncoder.encode(updateRequest.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        log.info("Successfully updated password for user: {}", user.getUsername());
    }
}
