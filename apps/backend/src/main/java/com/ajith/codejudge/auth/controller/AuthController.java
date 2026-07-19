package com.ajith.codejudge.auth.controller;

import com.ajith.codejudge.auth.dto.request.LoginRequest;
import com.ajith.codejudge.auth.dto.request.RegisterRequest;
import com.ajith.codejudge.auth.dto.request.ResetPasswordRequest;
import com.ajith.codejudge.auth.dto.request.TokenRefreshRequest;
import com.ajith.codejudge.auth.dto.request.UpdatePasswordRequest;
import com.ajith.codejudge.auth.dto.response.JwtResponse;
import com.ajith.codejudge.auth.dto.response.TokenRefreshResponse;
import com.ajith.codejudge.auth.service.interfaces.AuthService;
import com.ajith.codejudge.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for registration, login, logout, password resets and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new candidate user")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        authService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Please check your email for verification link."));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and generate JWT tokens")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.authenticateUser(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate expired Access Tokens using a valid Refresh Token")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout currently authenticated user and invalidate their Refresh Token")
    public ResponseEntity<ApiResponse<Void>> logout() {
        authService.logoutUser();
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify user email to activate the account")
    public ResponseEntity<ApiResponse<Void>> verify(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully. You can now login."));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request password reset verification link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset link sent to your email."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Update password using verification token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody UpdatePasswordRequest request) {
        authService.updatePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password updated successfully."));
    }
}
