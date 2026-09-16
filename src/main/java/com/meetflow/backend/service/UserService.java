package com.meetflow.backend.service;

import com.meetflow.backend.model.User;
import com.meetflow.backend.model.UserRole;
import com.meetflow.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User loginOrRegisterHost(String email, String name, String avatarUrl) {
        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> existingUser = userRepository.findByEmail(normalizedEmail);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                user.setAvatarUrl(avatarUrl);
            }
            if (name != null && !name.trim().isEmpty()) {
                user.setName(name.trim());
            }
            user.setRole(UserRole.HOST);
            return userRepository.save(user);
        }

        String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : extractNameFromEmail(normalizedEmail);
        String defaultAvatar = (avatarUrl != null && !avatarUrl.isEmpty()) 
                ? avatarUrl 
                : "https://ui-avatars.com/api/?name=" + displayName.replace(" ", "+") + "&background=A4613B&color=ffffff&bold=true";

        User newHost = User.builder()
                .email(normalizedEmail)
                .name(displayName)
                .role(UserRole.HOST)
                .avatarUrl(defaultAvatar)
                .build();

        return userRepository.save(newHost);
    }

    public User registerGuest(String name, String avatarUrl) {
        String displayName = (name != null && !name.trim().isEmpty()) ? name.trim() : "Invitado MeetFlow";
        String guestEmail = "guest_" + System.currentTimeMillis() + "@meetflow.guest";
        String defaultAvatar = (avatarUrl != null && !avatarUrl.isEmpty()) 
                ? avatarUrl 
                : "https://ui-avatars.com/api/?name=" + displayName.replace(" ", "+") + "&background=0284C7&color=ffffff&bold=true";

        User guestUser = User.builder()
                .email(guestEmail)
                .name(displayName)
                .role(UserRole.GUEST)
                .avatarUrl(defaultAvatar)
                .build();

        return userRepository.save(guestUser);
    }

    public User updateAvatar(Long userId, String avatarUrl) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setAvatarUrl(avatarUrl);
            return userRepository.save(user);
        }
        throw new RuntimeException("Usuario no encontrado con ID: " + userId);
    }

    private String extractNameFromEmail(String email) {
        String namePart = email.split("@")[0].replace(".", " ");
        if (namePart.isEmpty()) {
            return "Usuario MeetFlow";
        }
        return namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
    }
}
