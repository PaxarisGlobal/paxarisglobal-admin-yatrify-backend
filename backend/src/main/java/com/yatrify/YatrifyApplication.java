package com.yatrify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = MailHealthContributorAutoConfiguration.class)
@EnableCaching
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@EnableAsync
@EnableScheduling
public class YatrifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(YatrifyApplication.class, args);
    }
}
