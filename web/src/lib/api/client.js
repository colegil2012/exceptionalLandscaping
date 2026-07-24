/**
 * Thin wrapper over fetch for the Spring API.
 *
 * In dev, Vite proxies /api to localhost:8080 (see vite.config.js), so the
 * base is empty and the browser sees a single origin. In production set
 * PUBLIC_API_BASE to the deployed API origin.
 *
 * Uses $env/dynamic/public rather than $env/static/public so the value is read
 * at run time. That means changing the API origin on App Platform is an
 * environment-variable edit and a restart, not a rebuild.
 */

import { env } from '$env/dynamic/public';

const BASE = env.PUBLIC_API_BASE ?? '';

/** Error carrying the parsed body so callers can read field-level messages. */
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
    // Network-level failure: no response at all.
    throw new ApiError('Could not reach the server. Check your connection and try again.', 0, null);
  }

  const isJson = response.headers.get('content-type')?.includes('application/json');
  const body = isJson ? await response.json().catch(() => null) : null;

  if (!response.ok) {
    throw new ApiError(
      body?.message ?? 'Something went wrong. Try again in a moment.',
      response.status,
      body
    );
  }

  return body;
}

export const api = {
  /** @param {{random?: boolean, featured?: boolean, service?: string, tag?: string, limit?: number}} opts */
  gallery(opts = {}) {
    const params = new URLSearchParams();
    if (opts.random) params.set('random', 'true');
    if (opts.featured) params.set('featured', 'true');
    if (opts.service) params.set('service', opts.service);
    if (opts.tag) params.set('tag', opts.tag);
    if (opts.limit) params.set('limit', String(opts.limit));
    const qs = params.toString();
    return request(`/api/gallery${qs ? `?${qs}` : ''}`);
  },

  project(id) {
    return request(`/api/gallery/${encodeURIComponent(id)}`);
  },

  submitInquiry(payload) {
    return request('/api/contact', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
  }
};
