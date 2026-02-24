package com.cts.userservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Feign configuration to forward authentication headers to downstream services
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // Forward authentication headers
                    String userId = request.getHeader("X-User-Id");
                    String userRole = request.getHeader("X-User-Role");
                    String userEmail = request.getHeader("X-User-Email");
                    String authorization = request.getHeader("Authorization");

                    if (userId != null) {
                        template.header("X-User-Id", userId);
                    }
                    if (userRole != null) {
                        template.header("X-User-Role", userRole);
                    }
                    if (userEmail != null) {
                        template.header("X-User-Email", userEmail);
                    }
                    if (authorization != null) {
                        template.header("Authorization", authorization);
                    }
                }
            }
        };
    }
}

