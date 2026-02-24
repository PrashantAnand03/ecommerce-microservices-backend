package com.cts.cart.client;

import com.cts.cart.client.fallback.UserClientFallback;
import com.cts.cart.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", path = "/internal/users", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/{id}")
    UserDto getUser(@PathVariable("id") Long id);
}
