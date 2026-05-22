package com.yatrify.user.controller;

import com.yatrify.common.ApiResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import com.yatrify.user.dto.UpdateProfileRequest;
import com.yatrify.user.dto.UserProfileDto;
import com.yatrify.user.dto.UserSyncRequest;
import com.yatrify.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping("/sync")
    @Operation(summary = "Sync user from generic platform", description = "Creates or updates Yatrify-specific user profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> syncUser(@Valid @RequestBody UserSyncRequest request) {
        UserProfileDto dto = userProfileService.syncUser(request);
        return ResponseEntity.ok(ApiResponse.success("User synced successfully", dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(
            @AuthenticationPrincipal YatrifyUserPrincipal principal) {
        UserProfileDto dto = userProfileService.getByGenericUserId(principal.getGenericUserId());
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileDto dto = userProfileService.updateProfile(principal.getGenericUserId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", dto));
    }
}
