package com.ajith.codejudge.auth.service;

import com.ajith.codejudge.auth.dto.request.LoginRequest;
import com.ajith.codejudge.auth.dto.request.RegisterRequest;
import com.ajith.codejudge.auth.dto.response.JwtResponse;
import com.ajith.codejudge.auth.entity.RefreshToken;
import com.ajith.codejudge.auth.service.impl.AuthServiceImpl;
import com.ajith.codejudge.auth.service.interfaces.RefreshTokenService;
import com.ajith.codejudge.exception.ConflictException;
import com.ajith.codejudge.notification.service.interfaces.EmailService;
import com.ajith.codejudge.security.jwt.JwtProvider;
import com.ajith.codejudge.security.service.UserDetailsImpl;
import com.ajith.codejudge.user.entity.Role;
import com.ajith.codejudge.user.entity.User;
import com.ajith.codejudge.user.repository.RoleRepository;
import com.ajith.codejudge.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private Role candidateRole;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .username("testcandidate")
                .email("test@candidate.com")
                .password("password123")
                .firstName("Test")
                .lastName("Candidate")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("testcandidate")
                .password("password123")
                .build();

        candidateRole = Role.builder()
                .id(1L)
                .name("ROLE_CANDIDATE")
                .description("Candidate Role")
                .build();
    }

    @Test
    void registerUser_Success() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(roleRepository.findByName("ROLE_CANDIDATE")).thenReturn(Optional.of(candidateRole));
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        // Act
        authService.registerUser(registerRequest);

        // Assert
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("test@candidate.com"), anyString());
    }

    @Test
    void registerUser_DuplicateUsername_ThrowsConflictException() {
        // Arrange
        when(userRepository.existsByUsername(registerRequest.getUsername())).thenReturn(true);

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> authService.registerUser(registerRequest));
        assertNotNull(exception);
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void authenticateUser_Success() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        UserDetailsImpl userPrincipal = new UserDetailsImpl(
                1L, "testcandidate", "test@candidate.com", "encodedPassword", true, Collections.emptyList());

        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtProvider.generateJwtToken(authentication)).thenReturn("jwtAccessToken");

        RefreshToken mockRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("refreshTokenUuid")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();
        when(refreshTokenService.createRefreshToken(userPrincipal.getId())).thenReturn(mockRefreshToken);

        // Act
        JwtResponse response = authService.authenticateUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwtAccessToken", response.getAccessToken());
        assertEquals("refreshTokenUuid", response.getRefreshToken());
        assertEquals("testcandidate", response.getUsername());
        assertEquals("test@candidate.com", response.getEmail());
    }
}
