package com.meetflow.backend.controller;

import com.meetflow.backend.model.User;
import com.meetflow.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginHost(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String name = payload.get("name");
        String avatarUrl = payload.get("avatarUrl");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User host = userService.loginOrRegisterHost(email, name, avatarUrl);
        return ResponseEntity.ok(host);
    }

    @PostMapping("/guest")
    public ResponseEntity<User> registerGuest(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String avatarUrl = payload.get("avatarUrl");
        User guest = userService.registerGuest(name, avatarUrl);
        return ResponseEntity.ok(guest);
    }

    @PutMapping("/{id}/avatar")
    public ResponseEntity<User> updateAvatar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String avatarUrl = payload.get("avatarUrl");
        try {
            User updated = userService.updateAvatar(id, avatarUrl);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
