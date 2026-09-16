package com.meetflow.backend.service;

import com.meetflow.backend.model.User;
import com.meetflow.backend.model.UserRole;
import com.meetflow.backend.model.UserStatus;
import com.meetflow.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class WebmasterService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public WebmasterService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public User loginWebmaster(String email, String password) {
        String normalizedEmail = (email != null && !email.trim().isEmpty()) 
                ? email.trim().toLowerCase() 
                : "webmaster@meetflow.com";
        
        Optional<User> existing = userRepository.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            User wm = existing.get();
            wm.setRole(UserRole.WEBMASTER);
            wm.setStatus(UserStatus.APPROVED);
            return userRepository.save(wm);
        }

        User webmaster = User.builder()
                .email(normalizedEmail)
                .name("Webmaster Principal")
                .role(UserRole.WEBMASTER)
                .status(UserStatus.APPROVED)
                .avatarUrl("https://ui-avatars.com/api/?name=Webmaster+Admin&background=0D5A56&color=ffffff&bold=true")
                .build();

        return userRepository.save(webmaster);
    }

    public Map<String, String> sendHostInvitation(String email) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        String token = UUID.randomUUID().toString().substring(0, 8);
        String link = emailService.sendHostInvitationEmail(normalizedEmail, token);

        Map<String, String> response = new HashMap<>();
        response.put("email", normalizedEmail);
        response.put("token", token);
        response.put("activationLink", link);
        response.put("status", "INVITATION_SENT");
        return response;
    }

    public User applyForHost(String name, String email, String avatarUrl) {
        String normalizedEmail = email != null ? email.trim().toLowerCase() : "";
        String hostName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Anfitrión Postulante";
        String avatar = (avatarUrl != null && !avatarUrl.trim().isEmpty()) 
                ? avatarUrl 
                : "https://ui-avatars.com/api/?name=" + hostName.replace(" ", "+") + "&background=0D5A56&color=ffffff&bold=true";

        Optional<User> existing = userRepository.findByEmail(normalizedEmail);
        User host;
        if (existing.isPresent()) {
            host = existing.get();
            host.setName(hostName);
            host.setRole(UserRole.HOST);
            host.setStatus(UserStatus.PENDING_APPROVAL);
            host.setAvatarUrl(avatar);
        } else {
            host = User.builder()
                    .name(hostName)
                    .email(normalizedEmail)
                    .role(UserRole.HOST)
                    .status(UserStatus.PENDING_APPROVAL)
                    .avatarUrl(avatar)
                    .build();
        }

        return userRepository.save(host);
    }

    public List<User> getPendingHosts() {
        return userRepository.findByRoleAndStatus(UserRole.HOST, UserStatus.PENDING_APPROVAL);
    }

    public Optional<User> approveHost(Long hostId) {
        Optional<User> optionalUser = userRepository.findById(hostId);
        if (optionalUser.isPresent()) {
            User host = optionalUser.get();
            host.setStatus(UserStatus.APPROVED);
            return Optional.of(userRepository.save(host));
        }
        return Optional.empty();
    }

    public Optional<User> rejectHost(Long hostId) {
        Optional<User> optionalUser = userRepository.findById(hostId);
        if (optionalUser.isPresent()) {
            User host = optionalUser.get();
            host.setStatus(UserStatus.REJECTED);
            return Optional.of(userRepository.save(host));
        }
        return Optional.empty();
    }
}

