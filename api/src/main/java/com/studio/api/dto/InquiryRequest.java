package com.studio.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InquiryRequest(

        @NotBlank(message = "Enter your name")
        @Size(max = 120, message = "Name is too long")
        String name,

        @NotBlank(message = "Enter your email address")
        @Email(message = "Enter a valid email address")
        @Size(max = 200)
        String email,

        @NotBlank(message = "Enter a phone number so the crew can reach you")
        @Size(max = 40, message = "Phone number is too long")
        String phone,

        @NotBlank(message = "Enter the address of the property")
        @Size(max = 300, message = "Address is too long")
        String propertyAddress,

        @NotBlank(message = "Tell us what the property needs")
        @Size(max = 4000, message = "Message is too long")
        String message,

        String serviceType,
        String frequency,
        String urgency,
        String propertySize,

        /** Honeypot: must stay empty. */
        String website
) {}
