package com.cts.orderservice.client.fallback;

import com.cts.orderservice.client.UserClient;
import com.cts.orderservice.dto.UserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public UserDto getUserById(Long id) {
        log.warn("Fallback: User Service unavailable for user ID: {}", id);
        return null;
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.warn("Fallback: User Service unavailable for getting all users");
        return Collections.emptyList();
    }
}

