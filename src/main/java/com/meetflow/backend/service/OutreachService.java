package com.meetflow.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class OutreachService {

    private static final Logger log = LoggerFactory.getLogger(OutreachService.class);

    private final EmailService emailService;
    private final MeetingService meetingService;

    @Autowired
    public OutreachService(EmailService emailService, MeetingService meetingService) {
        this.emailService = emailService;
        this.meetingService = meetingService;
    }

    public Map<String, Object> procesarCampanaCsv(String nombreCampana, String meetingCode, String meetingTitle, String scheduledTime, MultipartFile file) throws Exception {
        return procesarCampanaCsv(nombreCampana, meetingCode, meetingTitle, scheduledTime, file, null);
    }

    public Map<String, Object> procesarCampanaCsv(String nombreCampana, String meetingCode, String meetingTitle, String scheduledTime, MultipartFile file, String baseUrl) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo CSV no puede estar vacío.");
        }

        List<GuestContact> contactos = new ArrayList<>();

        // Parse CSV lines
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Support both comma (,) and semicolon (;) separators
                String[] parts = line.split("[,;]");
                String email = "";
                String name = "Invitado";

                if (parts.length >= 2) {
                    // Could be Name, Email OR Email, Name
                    if (parts[0].contains("@")) {
                        email = parts[0].trim();
                        name = parts[1].trim();
                    } else if (parts[1].contains("@")) {
                        name = parts[0].trim();
                        email = parts[1].trim();
                    }
                } else if (parts.length == 1 && parts[0].contains("@")) {
                    email = parts[0].trim();
                }

                if (!email.isEmpty() && email.contains("@")) {
                    contactos.add(new GuestContact(name, email));
                }
            }
        }

        log.info("[OutreachService] Procesados {} contactos válidos desde CSV para la reunión {} ({}) a realizarse en {}", 
                contactos.size(), meetingTitle, meetingCode, scheduledTime);

        // Register / Ensure meeting exists in backend MeetingService
        meetingService.createMeeting(
                meetingTitle != null ? meetingTitle : "Reunión Agendada por Outreach",
                "Equipo " + (nombreCampana != null ? nombreCampana : "Outreach"),
                "Anfitrión MeetFlow",
                meetingCode
        );

        List<String> enviados = new ArrayList<>();
        List<String> fallidos = new ArrayList<>();
        // Send email invitations
        for (GuestContact contacto : contactos) {
            boolean enviado = emailService.sendGuestMeetingInvitation(
                    contacto.getEmail(),
                    contacto.getName(),
                    meetingCode,
                    meetingTitle != null ? meetingTitle : "Reunión Agendada MeetFlow",
                    scheduledTime != null ? scheduledTime : "Hoy, 16:30",
                    baseUrl
            );
            if (enviado) {
                enviados.add(contacto.getEmail());
            } else {
                fallidos.add(contacto.getEmail());
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        if (enviados.isEmpty() && !fallidos.isEmpty()) {
            resultado.put("mensaje", "Reunión registrada en tu Agenda, pero no se pudieron despachar los correos por error de autenticación SMTP con Gmail.");
        } else {
            resultado.put("mensaje", "Campaña procesada y reunión agendada exitosamente");
        }
        resultado.put("campana", nombreCampana);
        resultado.put("meetingCode", meetingCode);
        resultado.put("meetingTitle", meetingTitle);
        resultado.put("totalContactos", contactos.size());
        resultado.put("correosEnviados", enviados);
        resultado.put("correosFallidos", fallidos);

        return resultado;
    }

    public static class GuestContact {
        private String name;
        private String email;

        public GuestContact(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
    }
}
