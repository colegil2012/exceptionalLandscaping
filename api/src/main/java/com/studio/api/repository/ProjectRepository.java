package com.studio.api.repository;

import com.studio.api.model.Project;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {

    List<Project> findByFeaturedTrue();

    List<Project> findByServiceType(String serviceType);

    List<Project> findByTagsContaining(String tag);

    /** Server-side random sample for the homepage strip. */
    @Aggregation(pipeline = { "{ $sample: { size: ?0 } }" })
    List<Project> findRandom(int size);
}
