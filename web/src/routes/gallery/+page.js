import { api } from '$lib/api/client.js';

/**
 * Load the grouped gallery ({ albums, categories, projects, loose }) on the
 * server for first paint. On failure, return empty collections and a flag so
 * the page can show its error state.
 */
export async function load({ fetch }) {
    try {
        const grouped = await api.galleryGrouped({ fetch });
        return { ...grouped, loadError: false };
    } catch {
        return { albums: [], categories: [], projects: [], loose: [], loadError: true };
    }
}