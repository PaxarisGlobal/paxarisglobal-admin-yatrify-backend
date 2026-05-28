package com.yatrify.auth;

import com.yatrify.auth.dto.AuthResponse;
import com.yatrify.auth.dto.LoginRequest;
import com.yatrify.auth.dto.SignupRequest;
import com.yatrify.common.exception.BusinessException;
import com.yatrify.config.properties.YatrifyProperties;
import com.yatrify.integration.paxaris.PaxarisIdentityClient;
import com.yatrify.user.dto.UserSyncRequest;
import com.yatrify.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern NON_USERNAME = Pattern.compile("[^a-zA-Z0-9._-]");

    private final PaxarisIdentityClient paxarisIdentityClient;
    private final JwtTokenService jwtTokenService;
    private final UserProfileService userProfileService;
    private final YatrifyProperties properties;

    public AuthResponse signup(SignupRequest request) {
        String username = deriveUsername(request.getEmail());
        String defaultRole = properties.getPaxaris().getDefaultUserRole();

        paxarisIdentityClient.createUser(
                username,
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword());

        if (defaultRole != null && !defaultRole.isBlank()) {
            try {
                paxarisIdentityClient.assignRole(username, defaultRole);
            } catch (Exception e) {
                log.warn("Default role '{}' not assigned for '{}': {}", defaultRole, username, e.getMessage());
            }
        }

        AuthResponse auth = buildAuthResponse(username, request.getEmail(), request.getFirstName(), request.getLastName(),
                request.getPassword());
        syncLocalProfile(username, request.getEmail(), request.getFirstName(), request.getLastName(), request.getPhone());
        return auth;
    }

    public AuthResponse login(LoginRequest request) {
        String username = deriveUsername(request.getEmail());
        AuthResponse auth = buildAuthResponse(username, request.getEmail(), null, null, request.getPassword());
        syncLocalProfile(username, request.getEmail(), auth.getFirstName(), auth.getLastName(), null);
        return auth;
    }

    private void syncLocalProfile(String genericUserId, String email, String firstName, String lastName, String phone) {
        UserSyncRequest sync = new UserSyncRequest();
        sync.setGenericUserId(genericUserId);
        sync.setEmail(email);
        sync.setFirstName(firstName != null ? firstName : genericUserId);
        sync.setLastName(lastName);
        sync.setPhone(phone);
        userProfileService.syncUser(sync);
    }

    @SuppressWarnings("unchecked")
    private AuthResponse buildAuthResponse(
            String username,
            String email,
            String firstName,
            String lastName,
            String password) {
        Map<String, Object> loginBody = paxarisIdentityClient.login(username, password);

        List<String> roles = extractRoles(loginBody);
        if (roles.isEmpty()) {
            roles = List.of("USER");
        }

        String resolvedEmail = email != null ? email : username;
        String token = jwtTokenService.createToken(username, resolvedEmail, roles);

        return AuthResponse.builder()
                .token(token)
                .genericUserId(username)
                .email(resolvedEmail)
                .firstName(firstName != null ? firstName : username)
                .lastName(lastName)
                .roles(roles)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRoles(Map<String, Object> loginBody) {
        Object rolesObj = loginBody.get("roles");
        if (!(rolesObj instanceof List<?> list)) {
            return List.of();
        }
        List<String> roles = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                roles.add(item.toString());
            }
        }
        return roles;
    }

    /**
     * Called when the user submits the Become Organizer form ({@code POST /organizers/register}).
     * Assigns {@code PAXARIS_ORGANIZER_ROLE} via the same Paxo API as the Users / Assign Role tabs.
     */
    public void assignOrganizerRole(String keycloakUsername) {
        String organizerRole = properties.getPaxaris().getOrganizerRole();
        if (organizerRole == null || organizerRole.isBlank()) {
            throw new BusinessException(
                    "Organizer role is not configured. Set PAXARIS_ORGANIZER_ROLE (e.g. prodOrganizer).",
                    "ORGANIZER_ROLE_NOT_CONFIGURED");
        }
        paxarisIdentityClient.assignRole(keycloakUsername, organizerRole);
        log.info("Assigned organizer role '{}' to '{}' via Paxaris identity API", organizerRole, keycloakUsername);
    }

    /**
     * Merges organizer role names into an existing role list for Yatrify JWT/session.
     */
    public List<String> mergeOrganizerRoles(List<String> existingRoles) {
        List<String> updatedRoles = new ArrayList<>(existingRoles != null ? existingRoles : List.of());
        String organizerRole = properties.getPaxaris().getOrganizerRole();
        if (organizerRole != null && !organizerRole.isBlank() && !updatedRoles.contains(organizerRole)) {
            updatedRoles.add(organizerRole);
        }
        if (updatedRoles.stream().noneMatch(r -> r.toUpperCase().contains("ORGANIZER"))) {
            updatedRoles.add("ORGANIZER");
        }
        return updatedRoles;
    }

    /**
     * Keycloak username from email (local-part, sanitized).
     */
    public String deriveUsername(String email) {
        if (email == null || !email.contains("@")) {
            throw new BusinessException("Valid email is required for username", "INVALID_EMAIL");
        }
        String local = email.substring(0, email.indexOf('@')).toLowerCase(Locale.ROOT);
        String sanitized = NON_USERNAME.matcher(local).replaceAll(".");
        if (sanitized.isBlank()) {
            throw new BusinessException("Could not derive username from email", "INVALID_EMAIL");
        }
        return sanitized;
    }
}
