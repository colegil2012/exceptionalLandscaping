package com.studio.api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * A completed job. The gallery is a portfolio of finished work, so most
 * entries carry a before image alongside the finished one.
 */
@Document(collection = "project")
public class Project {

    @Id
    private String id;

    private String title;
    private String description;

    /** Neighbourhood or town, not a street address. */
    private String location;

    /** lawn-care, hardscape, planting, cleanup, irrigation, full-redesign */
    @Indexed
    private String serviceType;

    private List<String> tags;

    /** How long the job took, in plain words: "two days", "three weeks". */
    private String duration;

    private String imageKey;
    private String thumbKey;
    /** Optional before shot; null when the job had no meaningful "before". */
    private String beforeKey;

    private Integer width;
    private Integer height;
    private String lqip;

    @Indexed
    private boolean featured;

    private Instant completedAt;
    private Instant createdAt = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getImageKey() { return imageKey; }
    public void setImageKey(String imageKey) { this.imageKey = imageKey; }

    public String getThumbKey() { return thumbKey; }
    public void setThumbKey(String thumbKey) { this.thumbKey = thumbKey; }

    public String getBeforeKey() { return beforeKey; }
    public void setBeforeKey(String beforeKey) { this.beforeKey = beforeKey; }

    public Integer getWidth() { return width; }
    public void setWidth(Integer width) { this.width = width; }

    public Integer getHeight() { return height; }
    public void setHeight(Integer height) { this.height = height; }

    public String getLqip() { return lqip; }
    public void setLqip(String lqip) { this.lqip = lqip; }

    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
