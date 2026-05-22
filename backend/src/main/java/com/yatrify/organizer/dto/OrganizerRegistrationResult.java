package com.yatrify.organizer.dto;

import com.yatrify.auth.dto.AuthResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrganizerRegistrationResult {
    private OrganizerDto organizer;
    /** Updated session after prodOrganizer role is assigned in Keycloak */
    private AuthResponse auth;
}
