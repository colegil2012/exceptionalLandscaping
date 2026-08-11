import { api } from '$lib/api/client.js';

/**
 * Fetch the about-page meta so `data.meta` is populated for the component.
 * Universal load: runs on the server for first paint, then on the client for
 * subsequent navigations. Falls back to an empty object so the page still
 * renders its static chrome if the read fails.
 */
export async function load() {
    const meta = await api.meta();
    //console.log('aboutHeader raw:', JSON.stringify(meta.aboutHeader));
    return { meta };
}