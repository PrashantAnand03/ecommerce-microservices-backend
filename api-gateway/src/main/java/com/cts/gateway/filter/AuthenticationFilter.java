package com.cts.gateway.filter;

import com.cts.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            String method = request.getMethod().name();

            log.debug("Processing request: {} {}", method, path);

            // Skip authentication for open endpoints
            if (routeValidator.isOpenEndpoint(path)) {
                log.debug("Open endpoint, skipping authentication");
                return chain.filter(exchange);
            }

            // Check for Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Invalid Authorization header format");
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // Validate token
                if (!jwtUtil.isValidToken(token)) {
                    log.warn("Invalid or expired token");
                    return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
                }

                // Extract user details from token
                Long userId = jwtUtil.getUserIdFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                String email = jwtUtil.getEmailFromToken(token);

                log.info("Token validated - UserId: {}, Role: {}, Email: {}", userId, role, email);

                // Check role-based access
                if (!hasAccess(path, method, role)) {
                    log.warn("Access denied for user {} with role {} to {} {}", userId, role, method, path);
                    return onError(exchange, "Access denied. Insufficient permissions.", HttpStatus.FORBIDDEN);
                }

                // Add user info to headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(userId))
                        .header("X-User-Role", role)
                        .header("X-User-Email", email)
                        .header("Authorization", authHeader)
                        .build();

                log.debug("Headers added - X-User-Id: {}, X-User-Role: {}", userId, role);

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("Token validation failed: {}", e.getMessage());
                return onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
            }
        };
    }

    /**
     * Check if the user role has access to the requested path and HTTP method
     * Each endpoint has a clear case for easy understanding
     */
    private boolean hasAccess(String path, String method, String role) {

        // ╔══════════════════════════════════════════════════════════════════╗
        // ║                    ENDPOINT ACCESS CONTROL                       ║
        // ╠══════════════════════════════════════════════════════════════════╣
        // ║  /api/user/**  = USER only endpoints                             ║
        // ║  /api/admin/** = ADMIN only endpoints                            ║
        // ║  /api/both/**  = Both USER and ADMIN can access                  ║
        // ╚══════════════════════════════════════════════════════════════════╝

        // ─────────────────────────────────────────────────────────────────────
        // CASE 1: USER ONLY ENDPOINTS (/api/user/**)
        // Access: USER only
        // ─────────────────────────────────────────────────────────────────────
        if (path.startsWith("/api/user/")) {
            // /api/user/profile/** - Profile management (name, email, password, address, payment)
            // /api/user/cart/** - Cart operations (add, view, increment, decrement, remove)
            // /api/user/orders/** - Order operations (place order, view my orders, track)
            // /api/user/products/** - View products
            return isUser(role);
        }

        // ─────────────────────────────────────────────────────────────────────
        // CASE 2: ADMIN ONLY ENDPOINTS (/api/admin/**)
        // Access: ADMIN only
        // ─────────────────────────────────────────────────────────────────────
        if (path.startsWith("/api/admin/")) {
            // /api/admin/products/** - Product management (create, update, delete)
            // /api/admin/users/** - User management (get all, get by id, delete)
            // /api/admin/orders/** - Order management (view by user, update status, update payment)
            return isAdmin(role);
        }

        // ─────────────────────────────────────────────────────────────────────
        // CASE 3: BOTH USER AND ADMIN ENDPOINTS (/api/both/**)
        // Access: USER or ADMIN
        // ─────────────────────────────────────────────────────────────────────
        if (path.startsWith("/api/both/")) {
            // /api/both/products/** - View products (GET only)
            // /api/both/orders/** - Track orders
            return isUser(role) || isAdmin(role);
        }

        // ─────────────────────────────────────────────────────────────────────
        // CASE 4: DIRECT CART ENDPOINTS (/api/cart/**)
        // Access: USER only (direct cart operations bypassing user service)
        // ─────────────────────────────────────────────────────────────────────
        if (path.startsWith("/api/cart/")) {
            // /api/cart/add/** - Add to cart
            // /api/cart/view - View cart
            // /api/cart/increment/** - Increment item quantity
            // /api/cart/decrement/** - Decrement item quantity
            // /api/cart/remove/** - Remove from cart
            return isUser(role);
        }

        // ─────────────────────────────────────────────────────────────────────
        // CASE 5: DIRECT PRODUCT ENDPOINTS (/api/products/**)
        // Access: GET - Both USER and ADMIN, POST/PUT/DELETE - ADMIN only
        // ─────────────────────────────────────────────────────────────────────
        if (path.equals("/api/products") || path.startsWith("/api/products/")) {
            // GET requests - Both USER and ADMIN can view products
            if ("GET".equalsIgnoreCase(method)) {
                return isUser(role) || isAdmin(role);
            }
            // POST, PUT, DELETE - Only ADMIN can manage products
            return isAdmin(role);
        }

        // ─────────────────────────────────────────────────────────────────────
        // DEFAULT: Deny access if no case matches
        // ─────────────────────────────────────────────────────────────────────
        log.warn("No access rule defined for path: {} method: {}", path, method);
        return false;
    }

    /**
     * Check if role is USER
     */
    private boolean isUser(String role) {
        return "USER".equalsIgnoreCase(role);
    }

    /**
     * Check if role is ADMIN
     */
    private boolean isAdmin(String role) {
        return "ADMIN".equalsIgnoreCase(role);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");

        String body = String.format("{\"error\": \"%s\", \"status\": %d}", message, status.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}

