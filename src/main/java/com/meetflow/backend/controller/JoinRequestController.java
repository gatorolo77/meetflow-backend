package com.meetflow.backend.controller;

import com.meetflow.backend.model.JoinRequest;
import com.meetflow.backend.model.RequestStatus;
import com.meetflow.backend.service.JoinRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    public JoinRequestController(JoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<JoinRequest>> getPendingRequests() {
        return ResponseEntity.ok(joinRequestService.getPendingRequests());
    }

    @PostMapping
    public ResponseEntity<JoinRequest> createRequest(@RequestBody Map<String, String> payload) {
        String name = payload.get("name");
        String email = payload.getOrDefault("email", "invitado@meetflow.app");
        String code = payload.get("meetingCode");
        String title = payload.getOrDefault("meetingTitle", "Reunión MeetFlow");
        String avatarUrl = payload.get("avatarUrl");
        JoinRequest created = joinRequestService.createJoinRequest(name, email, code, title, avatarUrl);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<JoinRequest> approveRequest(@PathVariable Long id) {
        return joinRequestService.updateRequestStatus(id, RequestStatus.APPROVED)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<JoinRequest> rejectRequest(@PathVariable Long id) {
        return joinRequestService.updateRequestStatus(id, RequestStatus.REJECTED)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
