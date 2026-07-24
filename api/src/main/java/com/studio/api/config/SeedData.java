package com.studio.api.config;

import com.studio.api.model.Project;
import com.studio.api.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

/**
 * Populates the portfolio on first local run. Never active under "prod".
 */
@Configuration
@Profile("local")
public class SeedData {

    private static final Logger log = LoggerFactory.getLogger(SeedData.class);

    @Bean
    CommandLineRunner seedProjects(ProjectRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                log.info("Portfolio already seeded ({} projects)", repository.count());
                return;
            }

            List<Project> projects = List.of(
                    project("Full Front Yard Rebuild", "full-redesign", "Crescent Hill",
                            "three weeks", List.of("sod", "beds", "edging"), true, true,
                            "Removed a failing lawn down to bare soil, regraded for drainage away "
                          + "from the foundation, and laid fescue sod with cut beds along the walk."),
                    project("Limestone Retaining Wall", "hardscape", "Prospect",
                            "nine days", List.of("retaining-wall", "drainage", "stone"), true, true,
                            "Forty feet of dry-laid limestone holding back a slope that had been "
                          + "washing onto the driveway every spring. Gravel backfill and a drain "
                          + "line behind the full run."),
                    project("Weekly Mowing — Corporate Campus", "lawn-care", "Middletown",
                            "ongoing since 2021", List.of("mowing", "commercial", "edging"), false, true,
                            "Six acres on a Tuesday rotation, striped in alternating passes. "
                          + "Edging and blowing included every visit."),
                    project("Shade Garden Under Mature Oaks", "planting", "Cherokee Triangle",
                            "four days", List.of("perennials", "shade", "mulch"), true, false,
                            "Hostas, ferns, and heuchera under two oaks where turf had not grown "
                          + "in a decade. Hardwood mulch, no fabric."),
                    project("Paver Patio and Fire Pit", "hardscape", "St. Matthews",
                            "twelve days", List.of("patio", "pavers", "fire-pit"), true, true,
                            "Four hundred square feet of tumbled paver on compacted base, with a "
                          + "seat wall and a gas fire pit plumbed to the house line."),
                    project("Spring Cleanup and Bed Reset", "cleanup", "Highlands",
                            "two days", List.of("cleanup", "pruning", "mulch"), false, true,
                            "Cut back everything, pulled two seasons of volunteer maple, redefined "
                          + "the bed edges, and topped with three yards of mulch."),
                    project("Drip Irrigation Retrofit", "irrigation", "Anchorage",
                            "five days", List.of("irrigation", "drip", "zones"), false, false,
                            "Converted six spray zones to drip across the planting beds. Cut water "
                          + "use roughly in half without losing a plant."),
                    project("Backyard Drainage Correction", "hardscape", "Jeffersontown",
                            "six days", List.of("drainage", "french-drain", "grading"), true, true,
                            "A French drain and regrade for a yard that held standing water for "
                          + "days after any real rain. Daylighted the outlet at the back fence.")
            );

            repository.saveAll(projects);
            log.info("Seeded {} projects into the portfolio", projects.size());
        };
    }

    private Project project(String title, String serviceType, String location, String duration,
                            List<String> tags, boolean featured, boolean hasBefore,
                            String description) {
        Project p = new Project();
        p.setTitle(title);
        p.setServiceType(serviceType);
        p.setLocation(location);
        p.setDuration(duration);
        p.setTags(tags);
        p.setDescription(description);
        p.setFeatured(featured);
        p.setWidth(1600);
        p.setHeight(1067);

        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        p.setImageKey("projects/" + slug + ".jpg");
        p.setThumbKey("projects/" + slug + "-thumb.jpg");
        if (hasBefore) {
            p.setBeforeKey("projects/" + slug + "-before.jpg");
        }
        p.setLqip("data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciLz4=");
        return p;
    }
}
