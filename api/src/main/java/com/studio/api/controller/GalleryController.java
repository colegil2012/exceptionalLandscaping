package com.studio.api.controller;

import com.studio.api.dto.ProjectDto;
import com.studio.api.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final ProjectService projectService;

    public GalleryController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * GET /api/gallery
     * GET /api/gallery?random=true&limit=8    -> homepage strip
     * GET /api/gallery?featured=true
     * GET /api/gallery?service=hardscape
     * GET /api/gallery?tag=retaining-wall
     */
    @GetMapping
    public List<ProjectDto> list(
            @RequestParam(defaultValue = "false") boolean random,
            @RequestParam(defaultValue = "false") boolean featured,
            @RequestParam(name = "service", required = false) String serviceType,
            @RequestParam(required = false) String tag,
            @RequestParam(defaultValue = "12") int limit) {

        if (random) return projectService.findRandom(limit);
        if (featured) return projectService.findFeatured();
        if (serviceType != null && !serviceType.isBlank()) {
            return projectService.findByServiceType(serviceType);
        }
        if (tag != null && !tag.isBlank()) return projectService.findByTag(tag);
        return projectService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> byId(@PathVariable String id) {
        return projectService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
