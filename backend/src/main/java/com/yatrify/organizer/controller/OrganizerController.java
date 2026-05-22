package com.yatrify.organizer.controller;

import com.yatrify.common.ApiResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import com.yatrify.organizer.dto.CreateOrganizerRequest;
import com.yatrify.organizer.dto.OrganizerDto;
import com.yatrify.organizer.dto.OrganizerRegistrationResult;
import com.yatrify.organizer.service.OrganizerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/organizers")
@RequiredArgsConstructor
@Tag(name = "Organizers", description = "Organizer profile management APIs")
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping("/register")
    @Operation(summary = "Register as a trip organizer", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OrganizerRegistrationResult>> register(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody CreateOrganizerRequest request) {
        OrganizerRegistrationResult result = organizerService.registerOrganizer(principal, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Organizer profile created and role assigned. Please submit verification documents.",
                        result));
    }

    @GetMapping("/me")
    @Operation(summary = "Get my organizer profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OrganizerDto>> getMyProfile(
            @AuthenticationPrincipal YatrifyUserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.success(
                organizerService.getMyOrganizerProfile(principal.getGenericUserId())));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my organizer profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OrganizerDto>> updateProfile(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody CreateOrganizerRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                organizerService.updateOrganizer(principal.getGenericUserId(), request)));
    }

    @GetMapping("/{id}/public")
    @Operation(summary = "Get public organizer profile")
    public ResponseEntity<ApiResponse<OrganizerDto>> getPublicProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(organizerService.getPublicProfile(id)));
    }
}
