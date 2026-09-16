package com.meetflow.backend.config;

import com.meetflow.backend.model.*;
import com.meetflow.backend.repository.JoinRequestRepository;
import com.meetflow.backend.repository.MeetingRoomRepository;
import com.meetflow.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MeetingRoomRepository meetingRoomRepository;
    private final JoinRequestRepository joinRequestRepository;

    public DataInitializer(
            UserRepository userRepository,
            MeetingRoomRepository meetingRoomRepository,
            JoinRequestRepository joinRequestRepository) {
        this.userRepository = userRepository;
        this.meetingRoomRepository = meetingRoomRepository;
        this.joinRequestRepository = joinRequestRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Demo Registered Users
        User host = User.builder()
                .name("Sergio D.")
                .email("sergio.d@meetflow.com")
                .role(UserRole.HOST)
                .status(UserStatus.APPROVED)
                .avatarUrl("https://ui-avatars.com/api/?name=Sergio+D&background=0D5A56&color=ffffff")
                .build();
        userRepository.save(host);

        User participant1 = User.builder()
                .name("Ana García")
                .email("ana.garcia@meetflow.com")
                .role(UserRole.PARTICIPANT)
                .status(UserStatus.APPROVED)
                .avatarUrl("https://ui-avatars.com/api/?name=Ana+Garcia&background=A4613B&color=ffffff")
                .build();
        userRepository.save(participant1);

        User webmaster = User.builder()
                .name("Webmaster Principal")
                .email("webmaster@meetflow.com")
                .role(UserRole.WEBMASTER)
                .status(UserStatus.APPROVED)
                .avatarUrl("https://ui-avatars.com/api/?name=Webmaster+Admin&background=0D5A56&color=ffffff&bold=true")
                .build();
        userRepository.save(webmaster);

        // Seed Pending Host Registrations for Webmaster Approval
        User pendingHost1 = User.builder()
                .name("Roberto Mendoza")
                .email("roberto.mendoza@empresa.com")
                .role(UserRole.HOST)
                .status(UserStatus.PENDING_APPROVAL)
                .avatarUrl("https://ui-avatars.com/api/?name=Roberto+Mendoza&background=D97706&color=ffffff&bold=true")
                .build();
        userRepository.save(pendingHost1);

        User pendingHost2 = User.builder()
                .name("Valeria Soler")
                .email("valeria.soler@startup.io")
                .role(UserRole.HOST)
                .status(UserStatus.PENDING_APPROVAL)
                .avatarUrl("https://ui-avatars.com/api/?name=Valeria+Soler&background=2563EB&color=ffffff&bold=true")
                .build();
        userRepository.save(pendingHost2);

        // 2. Seed Initial Active Meeting Room
        MeetingRoom room = MeetingRoom.builder()
                .code("m-101")
                .title("Reunión de equipo - Sync Diario")
                .teamName("Equipo de Desarrollo")
                .hostName("Sergio D.")
                .isLive(true)
                .waitingRoomEnabled(true)
                .createdAt(LocalDateTime.now().minusMinutes(18))
                .build();
        meetingRoomRepository.save(room);

        // 3. Seed Pending Join Requests for Waiting Room
        JoinRequest req1 = JoinRequest.builder()
                .applicantName("Carlos Rodríguez")
                .applicantEmail("carlos.r@cliente.com")
                .targetMeetingCode("m-101")
                .targetMeetingTitle("Reunión de equipo - Sync Diario")
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now().minusMinutes(2))
                .build();
        joinRequestRepository.save(req1);

        JoinRequest req2 = JoinRequest.builder()
                .applicantName("Laura Martínez")
                .applicantEmail("laura.m@proveedor.com")
                .targetMeetingCode("m-101")
                .targetMeetingTitle("Reunión de equipo - Sync Diario")
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now().minusMinutes(5))
                .build();
        joinRequestRepository.save(req2);

        System.out.println("✅ Datos iniciales cargados en H2 Database para MeetFlow Backend.");
    }
}
