package com.studio.api.service;

import com.studio.api.dto.InquiryRequest;
import com.studio.api.model.Inquiry;
import com.studio.api.repository.InquiryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class InquiryService {

    private static final Logger log = LoggerFactory.getLogger(InquiryService.class);

    private final InquiryRepository repository;
    private final JavaMailSender mailSender;
    private final String notifyTo;
    private final String notifyFrom;
    private final boolean notificationsEnabled;

    public InquiryService(
            InquiryRepository repository,
            JavaMailSender mailSender,
            @Value("${app.mail.notify-to}") String notifyTo,
            @Value("${app.mail.notify-from}") String notifyFrom,
            @Value("${app.mail.enabled:false}") boolean notificationsEnabled) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.notifyTo = notifyTo;
        this.notifyFrom = notifyFrom;
        this.notificationsEnabled = notificationsEnabled;
    }

    public Inquiry submit(InquiryRequest request) {
        Inquiry inquiry = new Inquiry();
        inquiry.setName(request.name().trim());
        inquiry.setEmail(request.email().trim().toLowerCase());
        inquiry.setPhone(request.phone().trim());
        inquiry.setPropertyAddress(request.propertyAddress().trim());
        inquiry.setMessage(request.message().trim());
        inquiry.setServiceType(request.serviceType());
        inquiry.setFrequency(request.frequency());
        inquiry.setUrgency(request.urgency());
        inquiry.setPropertySize(request.propertySize());

        Inquiry saved = repository.save(inquiry);
        notify(saved);
        return saved;
    }

    /**
     * A dropped notification is recoverable; a lost lead is not. The inquiry is
     * already persisted by the time this runs, so failure is logged and swallowed.
     */
    private void notify(Inquiry inquiry) {
        if (!notificationsEnabled) {
            log.info("Mail disabled; inquiry {} stored without notification", inquiry.getId());
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(notifyTo);
            mail.setFrom(notifyFrom);
            mail.setReplyTo(inquiry.getEmail());
            mail.setSubject("%s request — %s".formatted(
                    orDash(inquiry.getServiceType()), inquiry.getName()));
            mail.setText("""
                    Name: %s
                    Phone: %s
                    Email: %s

                    Property: %s
                    Size: %s

                    Service: %s
                    Frequency: %s
                    Urgency: %s

                    %s
                    """.formatted(
                    inquiry.getName(),
                    inquiry.getPhone(),
                    inquiry.getEmail(),
                    inquiry.getPropertyAddress(),
                    orDash(inquiry.getPropertySize()),
                    orDash(inquiry.getServiceType()),
                    orDash(inquiry.getFrequency()),
                    orDash(inquiry.getUrgency()),
                    inquiry.getMessage()));
            mailSender.send(mail);
        } catch (MailException e) {
            log.error("Notification failed for inquiry {}", inquiry.getId(), e);
        }
    }

    private String orDash(String value) {
        return (value == null || value.isBlank()) ? "—" : value;
    }
}
