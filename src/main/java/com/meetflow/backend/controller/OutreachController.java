package com.meetflow.backend.controller;

import com.meetflow.backend.service.OutreachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/outreach")
public class OutreachController {

    private final OutreachService outreachService;

    @Autowired
    public OutreachController(OutreachService outreachService) {
        this.outreachService = outreachService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "nombreCampana", defaultValue = "Campaña Outreach") String nombreCampana,
            @RequestParam(value = "meetingCode", required = false) String meetingCode,
            @RequestParam(value = "meetingTitle", defaultValue = "Reunión MeetFlow") String meetingTitle,
            @RequestParam(value = "scheduledTime", defaultValue = "Hoy, 16:30") String scheduledTime,
            @RequestParam(value = "baseUrl", required = false) String baseUrlParam,
            jakarta.servlet.http.HttpServletRequest request
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El archivo CSV está vacío"));
        }

        if (meetingCode == null || meetingCode.trim().isEmpty()) {
            meetingCode = "outreach-" + Math.abs(nombreCampana.hashCode() % 10000);
        }

        String baseUrl = baseUrlParam;
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            baseUrl = request.getHeader("Origin");
        }
        if (baseUrl == null || baseUrl.trim().isEmpty()) {
            String referer = request.getHeader("Referer");
            if (referer != null && referer.startsWith("http")) {
                try {
                    java.net.URI uri = new java.net.URI(referer);
                    baseUrl = uri.getScheme() + "://" + uri.getAuthority();
                } catch (Exception ignored) {}
            }
        }

        try {
            Map<String, Object> resultado = outreachService.procesarCampanaCsv(nombreCampana, meetingCode.trim(), meetingTitle.trim(), scheduledTime.trim(), file, baseUrl);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "mensaje", "Error al procesar el CSV: " + e.getMessage()
            ));
        }
    }
}
