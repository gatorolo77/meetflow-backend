package com.meetflow.backend.controller;

import com.meetflow.backend.model.User;
import com.meetflow.backend.service.WebmasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/webmaster")
public class WebmasterController {

    private final WebmasterService webmasterService;

    public WebmasterController(WebmasterService webmasterService) {
        this.webmasterService = webmasterService;
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> payload) {
        String email = payload.getOrDefault("email", "webmaster@meetflow.com");
        String password = payload.get("password");
        User webmaster = webmasterService.loginWebmaster(email, password);
        return ResponseEntity.ok(webmaster);
    }

    @PostMapping("/hosts/invite")
    public ResponseEntity<Map<String, String>> inviteHost(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        return ResponseEntity.ok(webmasterService.sendHostInvitation(email));
    }

    @PostMapping("/hosts/apply")
    public ResponseEntity<User> applyForHost(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String email = payload.get("email");
        String avatarUrl = payload.get("avatarUrl");
        User pendingHost = webmasterService.applyForHost(name, email, avatarUrl);
        return ResponseEntity.ok(pendingHost);
    }

    @GetMapping("/hosts/pending")
    public ResponseEntity<List<User>> getPendingHosts() {
        return ResponseEntity.ok(webmasterService.getPendingHosts());
    }

    @PutMapping("/hosts/{id}/approve")
    public ResponseEntity<User> approveHost(@PathVariable Long id) {
        return webmasterService.approveHost(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/hosts/{id}/reject")
    public ResponseEntity<User> rejectHost(@PathVariable Long id) {
        return webmasterService.rejectHost(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
