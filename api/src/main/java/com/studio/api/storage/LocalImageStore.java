package com.studio.api.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Writes to a folder on disk and serves through the static resource handler
 * registered in WebConfig. Active under the "local" profile.
 */
@Component
@Profile("local")
public class LocalImageStore implements ImageStore {

    private final Path root;
    private final String publicPrefix;

    public LocalImageStore(
            @Value("${app.storage.local.root}") String root,
            @Value("${app.storage.local.public-prefix}") String publicPrefix) {
        this.root = Path.of(root).toAbsolutePath().normalize();
        this.publicPrefix = publicPrefix;
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create local image root: " + this.root, e);
        }
    }

    @Override
    public String put(String key, InputStream data, String contentType) {
        Path target = resolveSafely(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(data, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException("Write failed for key: " + key, e);
        }
        return key;
    }

    @Override
    public String urlFor(String key) {
        return publicPrefix + "/" + key;
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolveSafely(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Delete failed for key: " + key, e);
        }
    }

    /** Guards against path traversal via crafted keys. */
    private Path resolveSafely(String key) {
        Path candidate = root.resolve(key).normalize();
        if (!candidate.startsWith(root)) {
            throw new IllegalArgumentException("Key escapes storage root: " + key);
        }
        return candidate;
    }
}
