package com.meetflow.backend.service;

import com.meetflow.backend.model.JoinRequest;
import com.meetflow.backend.model.RequestStatus;
import com.meetflow.backend.repository.JoinRequestRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;

    public JoinRequestService(JoinRequestRepository joinRequestRepository) {
        this.joinRequestRepository = joinRequestRepository;
    }

    public List<JoinRequest> getPendingRequests() {
        return joinRequestRepository.findByStatus(RequestStatus.PENDING);
    }

    public JoinRequest createJoinRequest(String name, String email, String meetingCode, String meetingTitle, String avatarUrl) {
        JoinRequest request = JoinRequest.builder()
                .applicantName(name)
                .applicantEmail(email != null && !email.isEmpty() ? email : "invitado@meetflow.app")
                .targetMeetingCode(meetingCode)
                .targetMeetingTitle(meetingTitle)
                .avatarUrl(avatarUrl)
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();
        return joinRequestRepository.save(request);
    }

    public Optional<JoinRequest> updateRequestStatus(Long requestId, RequestStatus newStatus) {
        return joinRequestRepository.findById(requestId).map(request -> {
            request.setStatus(newStatus);
            return joinRequestRepository.save(request);
        });
    }
}
