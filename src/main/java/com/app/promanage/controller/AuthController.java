package com.app.promanage.controller;

import com.app.promanage.model.User;
import com.app.promanage.service.JwtService;
import com.app.promanage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        User savedUser = userService.register(user);
        String token = jwtService.generateToken(savedUser.getEmail());

        return ResponseEntity.ok(
                Map.of(
                        "token", token,
                        "user", Map.of(
                                "name", savedUser.getName(),
                                "email", savedUser.getEmail(),
                                "role", savedUser.getRole().getLevel()
                        )
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> login) {
        return userService.authenticate(login.get("email"), login.get("password"))
                .map(user -> ResponseEntity.ok(
                        Map.of(
                                "token", jwtService.generateToken(user.getEmail()),
                                "user", Map.of(
                                        "name", user.getName(),
                                        "email", user.getEmail(),
                                        "role", user.getRole().getLevel()
                                )
                        )
                ))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
}