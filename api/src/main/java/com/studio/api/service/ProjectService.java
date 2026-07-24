package com.studio.api.service;

import com.studio.api.dto.ProjectDto;
import com.studio.api.model.Project;
import com.studio.api.repository.ProjectRepository;
import com.studio.api.storage.ImageStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository repository;
    private final ImageStore imageStore;

    public ProjectService(ProjectRepository repository, ImageStore imageStore) {
        this.repository = repository;
        this.imageStore = imageStore;
    }

    public List<ProjectDto> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public List<ProjectDto> findFeatured() {
        return repository.findByFeaturedTrue().stream().map(this::toDto).toList();
    }

    public List<ProjectDto> findRandom(int limit) {
        int safeLimit = Math.clamp(limit, 1, 60);
        return repository.findRandom(safeLimit).stream().map(this::toDto).toList();
    }

    public List<ProjectDto> findByServiceType(String serviceType) {
        return repository.findByServiceType(serviceType).stream().map(this::toDto).toList();
    }

    public List<ProjectDto> findByTag(String tag) {
        return repository.findByTagsContaining(tag).stream().map(this::toDto).toList();
    }

    public Optional<ProjectDto> findById(String id) {
        return repository.findById(id).map(this::toDto);
    }

    private ProjectDto toDto(Project p) {
        return new ProjectDto(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getLocation(),
                p.getServiceType(),
                p.getTags(),
                p.getDuration(),
                p.getImageKey()  == null ? null : imageStore.urlFor(p.getImageKey()),
                p.getThumbKey()  == null ? null : imageStore.urlFor(p.getThumbKey()),
                p.getBeforeKey() == null ? null : imageStore.urlFor(p.getBeforeKey()),
                p.getWidth(),
                p.getHeight(),
                p.getLqip(),
                p.isFeatured()
        );
    }
}
