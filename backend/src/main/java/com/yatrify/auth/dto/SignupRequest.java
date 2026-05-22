package com.yatrify.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank
    private String firstName;

    private String lastName;

    @NotBlank
    @Email
    private String email;

    private String phone;

    @NotBlank
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;
}
