package com.app.promanage.controller;

import com.app.promanage.dto.UserSummaryDTO;
import com.app.promanage.model.User;
import com.app.promanage.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable UUID id) {
        return userService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<UserSummaryDTO> getUserSummary(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserSummary(id));
    }

    @PutMapping("/{id}/set-manager")
    public ResponseEntity<User> setManager(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.setManager(id));
    }

}