import { api } from '$lib/api/client.js';

/**
 * Load the album headers for the homepage "Recent jobs" strip on the server
 * (first paint + SEO). Falls back to an empty list so the section degrades to
 * its "no albums" state instead of erroring.
 */
export async function load({ fetch }) {
    try {
        return { albums: await api.albums({ fetch }) };
    } catch {
        return { albums: [] };
    }
}