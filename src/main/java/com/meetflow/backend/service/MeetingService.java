package com.meetflow.backend.service;

import com.meetflow.backend.model.MeetingRoom;
import com.meetflow.backend.repository.MeetingRoomRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class MeetingService {

    private final MeetingRoomRepository meetingRoomRepository;

    public MeetingService(MeetingRoomRepository meetingRoomRepository) {
        this.meetingRoomRepository = meetingRoomRepository;
    }

    public List<MeetingRoom> getAllMeetings() {
        return meetingRoomRepository.findAll();
    }

    public List<MeetingRoom> getLiveMeetings() {
        return meetingRoomRepository.findByIsLiveTrue();
    }

    public Optional<MeetingRoom> getMeetingByCode(String code) {
        return meetingRoomRepository.findByCode(code);
    }

    public MeetingRoom createMeeting(String title, String teamName, String hostName, String customCode) {
        String generatedCode = (customCode != null && !customCode.trim().isEmpty()) 
                ? customCode.trim() 
                : "m-" + UUID.randomUUID().toString().substring(0, 6);
        MeetingRoom room = MeetingRoom.builder()
                .code(generatedCode)
                .title(title)
                .teamName(teamName)
                .hostName(hostName)
                .isLive(true)
                .waitingRoomEnabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        return meetingRoomRepository.save(room);
    }
    
    public MeetingRoom createMeeting(String title, String teamName, String hostName) {
        return createMeeting(title, teamName, hostName, null);
    }

    public Optional<MeetingRoom> endMeeting(String code) {
        Optional<MeetingRoom> roomOpt = meetingRoomRepository.findByCode(code);
        if (roomOpt.isPresent()) {
            MeetingRoom room = roomOpt.get();
            room.setLive(false);
            return Optional.of(meetingRoomRepository.save(room));
        }
        return Optional.empty();
    }
}
