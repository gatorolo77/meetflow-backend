package com.meetflow.backend.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.client.url:https://meetflow-df071.web.app}")
    private String clientUrl;

    @Value("${spring.mail.username:rodrigodaremberg@gmail.com}")
    private String fromEmail;

    public String sendHostInvitationEmail(String toEmail, String token) {
        String activationLink = clientUrl + "/webmaster/login?email=" + toEmail + "&token=" + token;
        
        log.info("[MeetFlow Email] Generando correo de invitación de Anfitrión para: {} | Link: {}", toEmail, activationLink);

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(toEmail);
                helper.setSubject("¡Invitación a MeetFlow! Registra tu cuenta de Anfitrión");

                String htmlContent = "<html>" +
                        "<body style='font-family: Arial, sans-serif; background-color: #F2ECE0; color: #1E293B; padding: 30px;'>" +
                        "  <div style='max-width: 600px; margin: 0 auto; background-color: #FFFFFF; padding: 40px; border-radius: 20px; border: 1px solid #EAEFE9; box-shadow: 0 10px 25px rgba(0,0,0,0.05);'>" +
                        "    <div style='text-align: center; margin-bottom: 24px;'>" +
                        "      <h1 style='color: #0D5A56; margin: 0; font-size: 28px;'>MeetFlow</h1>" +
                        "      <span style='color: #A4613B; font-weight: bold; font-size: 14px;'>Plataforma de Videoconferencias</span>" +
                        "    </div>" +
                        "    <h2 style='color: #0D5A56; text-align: center;'>¡Hola! Has sido invitado como Anfitrión</h2>" +
                        "    <p style='font-size: 16px; line-height: 1.6; color: #475569;'>El Webmaster de MeetFlow te ha enviado un enlace exclusivo para solicitar tu cuenta de <strong>Anfitrión</strong>. Como Anfitrión podrás crear salas de reunión virtuales, gestionar accesos de participantes y coordinar eventos.</p>" +
                        "    <div style='text-align: center; margin: 35px 0;'>" +
                        "      <a href='" + activationLink + "' style='background-color: #0D5A56; color: #FFFFFF; padding: 16px 36px; font-weight: bold; font-size: 16px; text-decoration: none; border-radius: 14px; box-shadow: 0 6px 18px rgba(13,90,86,0.25); display: inline-block;'>Completar Registro de Anfitrión</a>" +
                        "    </div>" +
                        "    <p style='font-size: 14px; color: #64748B; text-align: center;'>Este enlace vence en 7 días y está vinculado a tu correo <strong>" + toEmail + "</strong>.</p>" +
                        "    <hr style='border: 0; border-top: 1px solid #E2E8F0; margin: 30px 0;'>" +
                        "    <p style='font-size: 12px; color: #94A3B8; text-align: center;'>Si el botón no abre automáticamente, copia este enlace en tu navegador:<br><a href='" + activationLink + "' style='color: #0D5A56;'>" + activationLink + "</a></p>" +
                        "  </div>" +
                        "</body>" +
                        "</html>";

                helper.setText(htmlContent, true);
                mailSender.send(message);

                log.info("[MeetFlow Email] Correo de invitación enviado exitosamente a: {}", toEmail);
            } catch (Exception e) {
                log.warn("[MeetFlow Email] No se pudo enviar el correo mediante SMTP (¿Propiedades no configuradas?): {}. El enlace es: {}", e.getMessage(), activationLink);
            }
        } else {
            log.info("[MeetFlow Email] JavaMailSender no configurado. Enlace de invitación generado: {}", activationLink);
        }

        return activationLink;
    }

    public boolean sendGuestMeetingInvitation(String toEmail, String guestName, String meetingCode, String meetingTitle, String scheduledTime) {
        return sendGuestMeetingInvitation(toEmail, guestName, meetingCode, meetingTitle, scheduledTime, null);
    }

    public boolean sendGuestMeetingInvitation(String toEmail, String guestName, String meetingCode, String meetingTitle, String scheduledTime, String customBaseUrl) {
        String base = (customBaseUrl != null && !customBaseUrl.trim().isEmpty()) ? customBaseUrl.trim() : clientUrl;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String safeName = (guestName != null && !guestName.trim().isEmpty()) ? guestName.trim() : "Invitado";
        String safeTime = (scheduledTime != null && !scheduledTime.trim().isEmpty()) ? scheduledTime.trim() : "Hoy, 16:30";
        String waitingRoomLink = base + "/login?mode=GUEST&code=" + meetingCode + "&guestName=" + java.net.URLEncoder.encode(safeName, java.nio.charset.StandardCharsets.UTF_8) + "&email=" + java.net.URLEncoder.encode(toEmail, java.nio.charset.StandardCharsets.UTF_8) + "&autoJoin=true";

        log.info("[MeetFlow Email Outreach] Invitación a reunión para: {} ({}) | Sala: {} | Horario: {} | Link: {}", safeName, toEmail, meetingCode, safeTime, waitingRoomLink);

        if (mailSender != null) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                try {
                    helper.setFrom(fromEmail, "MeetFlow Videoconferencias");
                } catch (Exception ex) {
                    helper.setFrom(fromEmail);
                }
                helper.setTo(toEmail);
                helper.setSubject("Invitación a Reunión: " + meetingTitle + " [" + safeTime + "]");

                String htmlContent = "<html>" +
                        "<body style='font-family: Arial, sans-serif; background-color: #F2ECE0; color: #1E293B; padding: 30px;'>" +
                        "  <div style='max-width: 600px; margin: 0 auto; background-color: #FFFFFF; padding: 40px; border-radius: 20px; border: 1px solid #EAEFE9; box-shadow: 0 10px 25px rgba(0,0,0,0.05);'>" +
                        "    <div style='text-align: center; margin-bottom: 24px;'>" +
                        "      <h1 style='color: #0D5A56; margin: 0; font-size: 28px;'>MeetFlow</h1>" +
                        "      <span style='color: #A4613B; font-weight: bold; font-size: 14px;'>Plataforma de Videoconferencias</span>" +
                        "    </div>" +
                        "    <h2 style='color: #0D5A56; text-align: center;'>¡Hola, " + safeName + "!</h2>" +
                        "    <p style='font-size: 16px; line-height: 1.6; color: #475569;'>Has sido invitado a participar en la reunión <strong>" + meetingTitle + "</strong>.</p>" +
                        "    <div style='background-color: #F8FAF9; border: 1px dashed #0D5A56; padding: 16px; border-radius: 12px; text-align: center; margin: 20px 0;'>" +
                        "      <span style='font-size: 13px; color: #64748B; display: block;'>Fecha y Horario:</span>" +
                        "      <strong style='font-size: 18px; color: #A4613B; display: block; margin-bottom: 8px;'>" + safeTime + "</strong>" +
                        "      <span style='font-size: 13px; color: #64748B; display: block;'>Código de la Reunión:</span>" +
                        "      <strong style='font-size: 22px; color: #0D5A56; letter-spacing: 1px;'>" + meetingCode + "</strong>" +
                        "    </div>" +
                        "    <div style='text-align: center; margin: 30px 0;'>" +
                        "      <a href='" + waitingRoomLink + "' style='background-color: #A4613B; color: #FFFFFF; padding: 16px 36px; font-weight: bold; font-size: 16px; text-decoration: none; border-radius: 14px; box-shadow: 0 6px 18px rgba(164,97,59,0.25); display: inline-block;'>Ingresar a la Sala de Espera</a>" +
                        "    </div>" +
                        "    <p style='font-size: 14px; color: #64748B; text-align: center;'>Al hacer clic serás redirigido a la sala de espera para aguardar la aprobación del anfitrión.</p>" +
                        "    <hr style='border: 0; border-top: 1px solid #E2E8F0; margin: 30px 0;'>" +
                        "    <p style='font-size: 12px; color: #94A3B8; text-align: center;'>También puedes ingresar ingresando directamente a MeetFlow y usando el código: <strong>" + meetingCode + "</strong>.<br>Enlace directo: <a href='" + waitingRoomLink + "' style='color: #0D5A56;'>" + waitingRoomLink + "</a></p>" +
                        "  </div>" +
                        "</body>" +
                        "</html>";

                helper.setText(htmlContent, true);
                mailSender.send(message);

                log.info("[MeetFlow Email Outreach] Correo enviado exitosamente a: {}", toEmail);
                return true;
            } catch (Exception e) {
                log.warn("[MeetFlow Email Outreach] No se pudo enviar el correo mediante SMTP (¿Propiedades no configuradas?): {}. El enlace generado es: {}", e.getMessage(), waitingRoomLink);
                return false;
            }
        } else {
            log.info("[MeetFlow Email Outreach] JavaMailSender no configurado. Enlace de sala de espera: {}", waitingRoomLink);
            return false;
        }
    }
}
