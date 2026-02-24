package com.cts.orderservice.client;

import com.cts.orderservice.client.fallback.UserClientFallback;
import com.cts.orderservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "USER-SERVICE", path = "/users", fallback = UserClientFallback.class)
public interface UserClient {

    /**
     * Retrieves user information by user ID.
     *
     * <p>Calls User-Service endpoint: GET /users/{id}
     *
     * @param id the unique identifier of the user
     * @return UserDto containing user details
     * @throws feign.FeignException if user not found or service unavailable
     */
    @GetMapping("/{id}")
    UserDto getUserById(@PathVariable("id") Long id);

    /**
     * Retrieves all users from User-Service.
     *
     * <p>Calls User-Service endpoint: GET /users
     *
     * <p><b>Note:</b> This method is provided for completeness but may not be
     * needed in typical order processing flows.
     *
     * @return List of all users
     */
    @GetMapping
    List<UserDto> getAllUsers();
}

