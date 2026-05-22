package com.yatrify.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request to sync user from the generic platform to Yatrify.
 * Called after successful authentication on the generic platform.
 */
@Data
public class UserSyncRequest {

    @NotBlank(message = "Generic user ID is required")
    private String genericUserId;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    @NotBlank(message = "First name is required")
    private String firstName;

    private String lastName;
    private String phone;
    private String profilePictureUrl;
}
