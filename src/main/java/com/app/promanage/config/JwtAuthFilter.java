package com.app.promanage.config;

import com.app.promanage.service.JwtService;
import com.app.promanage.service.UserService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements Filter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();

        // Skip JWT check for login and register endpoints
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtService.extractUsername(token);

            if (jwtService.validateToken(token, email)) {
                // Optionally, you can load UserDetails and set authentication here if needed
                request.setAttribute("userEmail", email);
            } else {
                // Token invalid - you may want to reject here (optional)
            }
        } else {
            // No auth header - you may want to reject here or allow to pass and be caught by Spring Security later
        }

        chain.doFilter(request, response);
    }
}