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

    public List<JoinRequest> getAllRequests() {
        return joinRequestRepository.findAll();
    }

    public JoinRequest createJoinRequest(String name, String email, String meetingCode, String meetingTitle, String avatarUrl) {
        String trimmedName = name != null ? name.trim() : "";
        Optional<JoinRequest> existing = joinRequestRepository.findAll().stream()
                .filter(r -> r.getApplicantName() != null && r.getApplicantName().equalsIgnoreCase(trimmedName) &&
                             r.getTargetMeetingCode() != null && r.getTargetMeetingCode().equalsIgnoreCase(meetingCode))
                .findFirst();
        if (existing.isPresent()) {
            JoinRequest req = existing.get();
            req.setStatus(RequestStatus.PENDING);
            req.setRequestedAt(LocalDateTime.now());
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                req.setAvatarUrl(avatarUrl);
            }
            if (meetingTitle != null && !meetingTitle.isEmpty()) {
                req.setTargetMeetingTitle(meetingTitle);
            }
            return joinRequestRepository.save(req);
        }

        JoinRequest request = JoinRequest.builder()
                .applicantName(trimmedName)
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

    public void removeApprovedRequest(String name, String meetingCode) {
        if (name == null || meetingCode == null) return;
        String cleanName = name.trim();
        List<JoinRequest> matches = joinRequestRepository.findAll().stream()
                .filter(r -> r.getApplicantName() != null && r.getApplicantName().equalsIgnoreCase(cleanName) &&
                             r.getTargetMeetingCode() != null &&
                             (r.getTargetMeetingCode().equalsIgnoreCase(meetingCode) || 
                              r.getTargetMeetingCode().contains(meetingCode) || 
                              meetingCode.contains(r.getTargetMeetingCode())))
                .toList();
        if (!matches.isEmpty()) {
            joinRequestRepository.deleteAll(matches);
        }
    }
}
