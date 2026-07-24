package com.studio.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * A service request. Unlike the artist site these are operational leads —
 * they carry a property address and an urgency flag so the crew can route
 * and schedule them.
 */
@Document(collection = "inquiry")
public class Inquiry {

    public enum Status { NEW, CONTACTED, QUOTED, SCHEDULED, WON, LOST }

    @Id
    private String id;

    private String name;
    private String email;
    private String phone;
    private String message;

    /** Street address of the property needing work. */
    private String propertyAddress;

    /** mowing, cleanup, hardscape, planting, irrigation, redesign, other */
    private String serviceType;

    /** weekly, biweekly, monthly, one-time */
    private String frequency;

    /** asap, this-month, this-season, planning-ahead */
    private String urgency;

    private String propertySize;

    @Indexed
    private Status status = Status.NEW;

    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPropertyAddress() { return propertyAddress; }
    public void setPropertyAddress(String propertyAddress) { this.propertyAddress = propertyAddress; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getPropertySize() { return propertySize; }
    public void setPropertySize(String propertySize) { this.propertySize = propertySize; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
