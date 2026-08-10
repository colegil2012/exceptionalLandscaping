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
  // Categories classify (Hardscaping, Mowing…); albums group a specific job.
  const categories = tagLabels.filter((t) => t.kind !== 'album');
  const albums = tagLabels.filter((t) => t.kind === 'album');
  const category = categories[0];
  return {
    id: img.id,
    title: img.caption || 'Untitled',
    imageUrl: img.imageUrl,
    thumbUrl: img.thumbUrl,
    lqip: img.lqip,
    serviceType: category ? category.slug : undefined,
    serviceLabel: category ? category.label : undefined,
    // All category/album memberships, so callers can filter/group either way:
    serviceTypes: categories.map((t) => t.slug),
    albumIds: albums.map((t) => t.id),
    albumSlugs: albums.map((t) => t.slug),
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

/** Splits the raw payload into sorted tag groups + a lookup, once. */
function normalize(data) {
  const tags = data?.tags ?? [];
  const images = data?.images ?? [];
  const tagsById = Object.fromEntries(tags.map((t) => [t.id, t]));

  const byPosition = (a, b) => a.position - b.position;
  const albums = tags.filter((t) => t.kind === 'album').sort(byPosition);
  const categories = tags.filter((t) => t.kind !== 'album').sort(byPosition);

  const projects = images
      .map((img) => toProject(img, tagsById))
      .sort((a, b) => a.position - b.position);

  return { albums, categories, projects, tagsById };
}

/**
 * The gallery payload rarely changes within a page load, but several components
 * (the gallery page, ProjectStrip, the homepage) all ask for it. We cache the
 * in-flight promise so they share a single network request; the resolved value
 * stays cached until refreshGallery() clears it.
 */
let galleryCache = null;

function fetchRawGallery({ refresh = false } = {}) {
  if (refresh) galleryCache = null;
  if (!galleryCache) {
    galleryCache = request(
        `/api/sites/${encodeURIComponent(SITE_SLUG)}/gallery`
    ).catch((err) => {
      // Don't cache failures — let the next caller retry.
      galleryCache = null;
      throw err;
    });
  }
  return galleryCache;
}

/** Force the next gallery read to hit the network (e.g. after an admin edit). */
export function refreshGallery() {
  galleryCache = null;
}

export const api = {
  /**
   * @param {{random?: boolean, limit?: number, tag?: string, album?: string}} opts
   * Fetches the site's gallery from the Celtech API and returns a flat,
   * project-shaped array so existing components need no structural change.
   */
  async gallery(opts = {}) {
    const data = await fetchRawGallery();
    const { albums, categories, projects } = normalize(data);

    let result = projects;

    // Client-side equivalents of the old query params:
    if (opts.tag) {
      const wanted = categories.find((t) => t.slug === opts.tag)?.id;
      result = result.filter((p) => p.serviceTypes.includes(opts.tag) && wanted);
    }
    if (opts.album) {
      const wanted = albums.find((t) => t.slug === opts.album)?.id;
      result = result.filter((p) => wanted && p.albumIds.includes(wanted));
    }
    if (opts.random) {
      result = [...result].sort(() => Math.random() - 0.5);
    }
    if (opts.limit) {
      result = result.slice(0, opts.limit);
    }
    return result;
  },

  /**
   * Album cards for the gallery landing, sorted by their managed position.
   * Each album carries its cover image (resolved from coverImageId, falling
   * back to the album's first image) and a count.
   */
  async albums() {
    const data = await fetchRawGallery();
    const { albums, projects } = normalize(data);

    return albums.map((album) => {
      const members = projects.filter((p) => p.albumIds.includes(album.id));
      const cover =
          members.find((p) => p.id === album.coverImageId) ?? members[0] ?? null;
      return {
        id: album.id,
        slug: album.slug,
        label: album.label,
        position: album.position,
        count: members.length,
        coverUrl: cover?.thumbUrl ?? cover?.imageUrl,
        coverLqip: cover?.lqip
      };
    });
  },

  /**
   * The whole gallery in one shot, already normalized and sorted:
   * { albums, categories, projects }. Lets a page group by album or filter by
   * category without re-deriving anything from the two raw arrays.
   */
  async galleryGrouped() {
    const data = await fetchRawGallery();
    const { albums, categories, projects } = normalize(data);

    const withMembers = albums.map((album) => ({
      id: album.id,
      slug: album.slug,
      label: album.label,
      position: album.position,
      coverImageId: album.coverImageId,
      images: projects.filter((p) => p.albumIds.includes(album.id))
    }));

    // Images that belong to no album, so nothing gets orphaned on the page.
    const loose = projects.filter((p) => p.albumIds.length === 0);

    return { albums: withMembers, categories, projects, loose };
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