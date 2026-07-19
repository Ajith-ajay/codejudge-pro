package com.ajith.codejudge.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    @Builder.Default
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
}
