package com.meetflow.backend.controller;

import com.meetflow.backend.model.MeetingRoom;
import com.meetflow.backend.service.MeetingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<List<MeetingRoom>> getAllMeetings() {
        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/live")
    public ResponseEntity<List<MeetingRoom>> getLiveMeetings() {
        return ResponseEntity.ok(meetingService.getLiveMeetings());
    }

    @GetMapping("/{code}")
    public ResponseEntity<MeetingRoom> getMeetingByCode(@PathVariable String code) {
        return meetingService.getMeetingByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MeetingRoom> createMeeting(@RequestBody Map<String, String> payload) {
        String title = payload.getOrDefault("title", "Nueva Sesión MeetFlow");
        String team = payload.getOrDefault("team", "General");
        String host = payload.getOrDefault("hostName", "Sergio D.");
        String code = payload.get("code");
        MeetingRoom created = meetingService.createMeeting(title, team, host, code);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{code}/end")
    public ResponseEntity<MeetingRoom> endMeeting(@PathVariable String code) {
        return meetingService.endMeeting(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
