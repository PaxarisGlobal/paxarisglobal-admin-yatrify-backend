package com.yatrify.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "yatrify")
public class YatrifyProperties {

    private GenericPlatform genericPlatform = new GenericPlatform();
    private PaxarisIdentity paxaris = new PaxarisIdentity();
    private Jwt jwt = new Jwt();
    private Aws aws = new Aws();
    private OpenAi openai = new OpenAi();
    private Razorpay razorpay = new Razorpay();
    private Cors cors = new Cors();

    @Data
    public static class GenericPlatform {
        private String baseUrl;
        private String apiKey;
        private String productCode;
    }

    /**
     * Paxaris API Gateway + product-integration (external product app-to-app) settings.
     */
    @Data
    public static class PaxarisIdentity {
        /** e.g. http://host.docker.internal:8085 or http://api-gateway:8085 */
        private String gatewayUrl = "http://127.0.0.1:8085";
        private String realm = "paxarisglobal";
        /** Keycloak client_id for Yatrify (product-integration + end-user login) */
        private String productId = "yatrify";
        /** Product role assigned on signup */
        private String defaultUserRole = "user";
        /** Role assigned when user becomes organizer */
        private String organizerRole = "prodOrganizer";
    }

    @Data
    public static class Jwt {
        private String secret;
        private long expiration;
    }

    @Data
    public static class Aws {
        private String accessKey;
        private String secretKey;
        private String region;
        private String s3Bucket;
    }

    @Data
    public static class OpenAi {
        private String apiKey;
        private String model;
        private int maxTokens;
    }

    @Data
    public static class Razorpay {
        private String keyId;
        private String keySecret;
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins;
    }
}
