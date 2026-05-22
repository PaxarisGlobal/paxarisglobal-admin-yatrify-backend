package com.yatrify.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String genericUserId;
    private String email;
    private String firstName;
    private String lastName;
    private List<String> roles;
}
