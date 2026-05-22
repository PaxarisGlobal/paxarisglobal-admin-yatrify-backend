package com.yatrify.config.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class YatrifyUserPrincipal {
    private String genericUserId;
    private String email;
    private List<String> roles;

    public boolean hasRole(String role) {
        return roles != null && roles.contains(role.toUpperCase());
    }

    public boolean isOrganizer() {
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> {
            String upper = r.toUpperCase();
            return upper.contains("ORGANIZER") || upper.equals("PRODORGANIZER");
        });
    }

    public boolean isAdmin() {
        return hasRole("ADMIN") || hasRole("YATRIFY_ADMIN");
    }
}
