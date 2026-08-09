/**
 * Ells frontend API client — now consumes the centralized Celtech API.
 *
 * All data comes from https://celtechsolutions.tech/api. Gallery reads are
 * public + cached; the contact form POSTs leads to the centralized inquiry
 * endpoint. This site no longer has its own Spring API or database.
 *
 * PUBLIC_API_BASE must be set to the Celtech API origin in production, since
 * this is now a cross-origin call. In dev, point it at your local API.
 */

import { env } from '$env/dynamic/public';

const BASE = env.PUBLIC_API_BASE ?? 'https://celtechsolutions.tech';

/** This site's slug on the Celtech platform (the Spaces folder + URL segment). */
const SITE_SLUG = env.PUBLIC_SITE_SLUG ?? 'ells';

export class ApiError extends Error {
  constructor(message, status, body) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${BASE}${path}`, {
      headers: { 'Content-Type': 'application/json', ...options.headers },
      ...options
    });
  } catch {
    throw new ApiError('Could not reach the server. Check your connection and try again.', 0, null);
  }
  const isJson = response.headers.get('content-type')?.includes('application/json');
  const body = isJson ? await response.json().catch(() => null) : null;
  if (!response.ok) {
    throw new ApiError(body?.message ?? 'Something went wrong. Try again in a moment.', response.status, body);
  }
  return body;
}

/**
 * Maps a Celtech GalleryImage DTO into the "project" shape the ells components
 * expect. Fields the centralized model doesn't have (location, duration,
 * before/after, width/height) are left undefined; the components already guard
 * for those with optional chaining, so they degrade gracefully.
 */
function toProject(img, tagsById) {
  const tagLabels = (img.tagIds ?? []).map((id) => tagsById[id]).filter(Boolean);
  // Use the first category-kind tag as the "serviceType" for filtering.
  const category = tagLabels.find((t) => t.kind !== 'album');
  return {
    id: img.id,
    title: img.caption || 'Untitled',
    imageUrl: img.imageUrl,
    thumbUrl: img.thumbUrl,
    lqip: img.lqip,
    serviceType: category ? category.slug : undefined,
    serviceLabel: category ? category.label : undefined,
    position: img.position,
    // Not yet in the centralized model — undefined is fine, components guard for it:
    location: undefined,
    duration: undefined,
    beforeUrl: undefined,
    description: img.altText || undefined,
    width: undefined,
    height: undefined
  };
}

export const api = {
  /**
   * @param {{random?: boolean, limit?: number, tag?: string}} opts
   * Fetches the site's gallery from the Celtech API and returns a flat,
   * project-shaped array so existing components need no structural change.
   */
  async gallery(opts = {}) {
    const data = await request(`/api/sites/${encodeURIComponent(SITE_SLUG)}/gallery`);
    const tags = data?.tags ?? [];
    const images = data?.images ?? [];
    const tagsById = Object.fromEntries(tags.map((t) => [t.id, t]));

    let projects = images.map((img) => toProject(img, tagsById));

    // Client-side equivalents of the old query params:
    if (opts.tag) {
      const wanted = tags.find((t) => t.slug === opts.tag)?.id;
      projects = projects.filter((p) =>
          images.find((i) => i.id === p.id)?.tagIds?.includes(wanted));
    }
    if (opts.random) {
      projects = [...projects].sort(() => Math.random() - 0.5);
    }
    if (opts.limit) {
      projects = projects.slice(0, opts.limit);
    }
    return projects;
  },

  /** Single project by id — resolved from the same gallery payload. */
  async project(id) {
    const all = await this.gallery();
    return all.find((p) => p.id === id) ?? null;
  },

  /** Contact form → centralized inquiry endpoint for this site. */
  submitInquiry(payload) {
    return request(`/api/sites/${encodeURIComponent(SITE_SLUG)}/inquiries`, {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  }
};