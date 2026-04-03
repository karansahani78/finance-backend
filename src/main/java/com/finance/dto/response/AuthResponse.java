package com.finance.dto.response;

import com.finance.model.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String token;
    private String type;
    private Long userId;
    private String name;
    private String email;
    private Role role;

    public static AuthResponse of(String token, Long userId, String name, String email, Role role) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(userId)
                .name(name)
                .email(email)
                .role(role)
                .build();
    }
}
