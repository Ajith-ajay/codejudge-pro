package com.ajith.codejudge.auth.service.interfaces;

import com.ajith.codejudge.auth.dto.request.LoginRequest;
import com.ajith.codejudge.auth.dto.request.RegisterRequest;
import com.ajith.codejudge.auth.dto.request.ResetPasswordRequest;
import com.ajith.codejudge.auth.dto.request.TokenRefreshRequest;
import com.ajith.codejudge.auth.dto.request.UpdatePasswordRequest;
import com.ajith.codejudge.auth.dto.response.JwtResponse;
import com.ajith.codejudge.auth.dto.response.TokenRefreshResponse;

public interface AuthService {

    void registerUser(RegisterRequest registerRequest);

    JwtResponse authenticateUser(LoginRequest loginRequest);

    TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest);

    void logoutUser();

    void verifyEmail(String token);

    void requestPasswordReset(ResetPasswordRequest resetRequest);

    void updatePassword(UpdatePasswordRequest updateRequest);
}
