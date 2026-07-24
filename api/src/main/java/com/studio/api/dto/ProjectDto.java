package com.studio.api.dto;

import java.util.List;

/** Storage keys resolved to URLs; the client never sees bucket paths. */
public record ProjectDto(
        String id,
        String title,
        String description,
        String location,
        String serviceType,
        List<String> tags,
        String duration,
        String imageUrl,
        String thumbUrl,
        String beforeUrl,
        Integer width,
        Integer height,
        String lqip,
        boolean featured
) {}
