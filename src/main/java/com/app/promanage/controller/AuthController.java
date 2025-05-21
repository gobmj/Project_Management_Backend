package com.app.promanage.controller;

import com.app.promanage.service.JwtService;
import com.app.promanage.model.User;
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
    public ResponseEntity<User> register(@RequestBody User user) {
        return ResponseEntity.ok(userService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> login) {
        return userService.authenticate(login.get("email"), login.get("password"))
                .map(user -> ResponseEntity.ok(Map.of("token", jwtService.generateToken(user.getEmail()))))
                .orElse(ResponseEntity.status(401).body(Map.of("error", "Invalid credentials")));
    }
}