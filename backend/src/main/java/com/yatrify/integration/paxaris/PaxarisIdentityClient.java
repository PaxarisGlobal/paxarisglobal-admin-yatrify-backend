package com.yatrify.integration.paxaris;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yatrify.common.exception.BusinessException;
import com.yatrify.config.properties.YatrifyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calls Paxaris external product-integration APIs (app-to-app) via API Gateway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaxarisIdentityClient {

    private final RestTemplate restTemplate;
    private final YatrifyProperties properties;
    private final ObjectMapper objectMapper;

    public void createUser(String username, String email, String firstName, String lastName, String password) {
        YatrifyProperties.PaxarisIdentity cfg = properties.getPaxaris();
        String url = integrationUrl(cfg, "/users");

        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("firstName", firstName);
        body.put("lastName", lastName != null ? lastName : "");
        body.put("enabled", true);
        body.put("credentials", List.of(Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        )));

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders()), Map.class);
            log.info("Paxaris: created user '{}' for product '{}' in realm '{}'", username, cfg.getProductId(), cfg.getRealm());
        } catch (HttpClientErrorException.Conflict e) {
            log.info("Paxaris: user '{}' already exists in realm '{}'", username, cfg.getRealm());
        } catch (ResourceAccessException e) {
            throw new BusinessException(
                    "Cannot reach Paxaris gateway at " + cfg.getGatewayUrl()
                            + ". Start paxo/scripts/start-local-access.sh and set PAXARIS_GATEWAY_URL=http://host.docker.internal:8085",
                    "PAXARIS_UNREACHABLE");
        } catch (HttpClientErrorException e) {
            throw paxarisError("create user", e);
        }
    }

    public void assignRole(String username, String roleName) {
        YatrifyProperties.PaxarisIdentity cfg = properties.getPaxaris();
        String url = integrationUrl(cfg, "/users/" + username + "/roles");
        List<Map<String, String>> body = List.of(Map.of("name", roleName));

        try {
            restTemplate.postForEntity(url, new HttpEntity<>(body, jsonHeaders()), Map.class);
            log.info("Paxaris: assigned role '{}' to user '{}' on product '{}'", roleName, username, cfg.getProductId());
        } catch (ResourceAccessException e) {
            throw new BusinessException(
                    "Cannot reach Paxaris gateway at " + cfg.getGatewayUrl(),
                    "PAXARIS_UNREACHABLE");
        } catch (HttpClientErrorException e) {
            throw paxarisError("assign role", e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> login(String username, String password) {
        YatrifyProperties.PaxarisIdentity cfg = properties.getPaxaris();
        String url = gatewayBase(cfg) + "/identity/" + cfg.getRealm().trim() + "/login";

        Map<String, String> body = Map.of(
                "username", username,
                "password", password,
                "client_id", cfg.getProductId().trim()
        );

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, new HttpEntity<>(body, jsonHeaders()), Map.class);
            if (response.getBody() == null) {
                throw new BusinessException("Empty login response from Paxaris", "PAXARIS_LOGIN_FAILED");
            }
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new BusinessException(
                    "Cannot reach Paxaris gateway at " + cfg.getGatewayUrl(),
                    "PAXARIS_UNREACHABLE");
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.Forbidden e) {
            throw new BusinessException("Invalid email or password", "INVALID_CREDENTIALS",
                    org.springframework.http.HttpStatus.UNAUTHORIZED);
        } catch (HttpClientErrorException e) {
            throw paxarisError("login", e);
        }
    }

    private String integrationUrl(YatrifyProperties.PaxarisIdentity cfg, String suffix) {
        return gatewayBase(cfg)
                + "/identity/product-integration/"
                + cfg.getRealm().trim()
                + "/products/"
                + cfg.getProductId().trim()
                + suffix;
    }

    private String gatewayBase(YatrifyProperties.PaxarisIdentity cfg) {
        String base = cfg.getGatewayUrl();
        if (base.endsWith("/")) {
            return base.substring(0, base.length() - 1);
        }
        return base;
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private BusinessException paxarisError(String action, HttpClientErrorException e) {
        String detail = e.getResponseBodyAsString();
        log.error("Paxaris {} failed: {} {}", action, e.getStatusCode(), detail);
        try {
            Map<String, Object> parsed = objectMapper.readValue(detail, new TypeReference<>() {});
            Object message = parsed.get("message");
            if (message != null) {
                return new BusinessException("Paxaris " + action + " failed: " + message, "PAXARIS_ERROR");
            }
        } catch (Exception ignored) {
            // use raw body
        }
        return new BusinessException("Paxaris " + action + " failed: " + detail, "PAXARIS_ERROR");
    }
}
