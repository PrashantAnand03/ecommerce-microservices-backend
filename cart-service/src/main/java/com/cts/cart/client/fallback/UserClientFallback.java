package com.cts.cart.client.fallback;

import com.cts.cart.client.UserClient;
import com.cts.cart.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public UserDto getUser(Long id) {
        log.warn("Fallback: User Service unavailable for user ID: {}", id);
        return null;
    }
}

