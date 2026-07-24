package com.studio.api.storage;

import java.io.InputStream;

/**
 * Abstraction over image persistence. Controllers and services depend on this
 * interface only; the active implementation is chosen by Spring profile.
 */
public interface ImageStore {

    /** Persist bytes under the given key. Returns the key that was written. */
    String put(String key, InputStream data, String contentType);

    /** Resolve a publicly reachable URL for a stored key. */
    String urlFor(String key);

    /** Remove an object. No-op if the key is absent. */
    void delete(String key);
}
