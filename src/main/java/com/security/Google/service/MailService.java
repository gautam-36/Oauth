package com.security.Google.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    JavaMailSender mailSender;

    public void sendMail(String mailTo, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailTo);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}
