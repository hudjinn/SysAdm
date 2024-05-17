package br.com.sysadm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmailController {

    @Autowired
    private JavaMailSender emailSender;

    @PostMapping("/enviar-email")
    public String enviarEmail(
            @RequestParam String emailDestino,
            @RequestParam String assunto,
            @RequestParam String texto
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailDestino);
        message.setSubject(assunto);
        message.setText(texto);
        emailSender.send(message);
        return "E-mail enviado com sucesso!";
    }
}
