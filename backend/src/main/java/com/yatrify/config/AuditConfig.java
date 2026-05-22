package com.yatrify.config;

import com.yatrify.config.security.YatrifyUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            if (authentication.getPrincipal() instanceof YatrifyUserPrincipal principal) {
                return Optional.of(principal.getGenericUserId());
            }
            return Optional.of(authentication.getName());
        };
    }
}
