# Exceptional Landscaping & Lawn Services

Portfolio and service-request site for a Louisville landscaping business.

This repo is **only the frontend** — a SvelteKit app that consumes the
centralized **Celtech API** (`celtechsolutions.tech`) for all of its content:
the gallery, the about-page copy, and lead capture. There is no backend or
database in this repo; content (including images) is managed in the Celtech
admin portal and read here at request time.

- **Frontend** — SvelteKit 2 / Svelte 5 (runes), Vite
- **Runtime** — Node via `@sveltejs/adapter-node`
- **Data source** — the centralized Celtech API (gallery, site meta, inquiries)

---

## Layout

```
exceptionalLandscaping/
├── .do/
│   └── app.yaml               DigitalOcean App Platform spec
├── web/                       SvelteKit application (everything lives here)
│   ├── src/
│   │   ├── lib/
│   │   │   ├── api/client.js   Fetch wrapper + mappers for the Celtech API
│   │   │   ├── components/     Component + its own CSS, side by side
│   │   │   ├── styles/         Design tokens (root.css) and shared primitives
│   │   │   └── safehtml.js     Allow-list HTML sanitizer for portal copy
│   │   ├── routes/             One folder per page, each with its page.css
│   │   │   ├── +layout.svelte  Site chrome (nav + footer) around every page
│   │   │   ├── +page.svelte    Home (hero, services, album strip)
│   │   │   ├── +page.js        Loads album headers for the home strip
│   │   │   ├── bio/            About page (+page.svelte, +page.js, page.css)
│   │   │   ├── gallery/        Gallery (+page.svelte, +page.js, page.css)
│   │   │   └── contact/        Quote / contact form
│   │   ├── scripts/            Page logic + Svelte actions (e.g. mowline)
│   │   └── app.html            HTML shell
│   ├── static/                 Static assets served as-is
│   ├── Dockerfile             The image built and deployed
│   ├── svelte.config.js
│   ├── vite.config.js
│   └── package.json
├── DEPLOYMENT.md
└── README.md
```

> The root-level `docker-compose.yml` and `.dockerignore` are legacy and no
> longer used — the only active container build is `web/Dockerfile`.

### Where styles live

`src/lib/styles/root.css` holds every design token — colors, type scale,
spacing, motion timings — plus the reset and shared primitives (`.btn`,
`.shell`, `.eyebrow`, `.mowline`). Nothing downstream hardcodes a literal
value.

Each component imports its own stylesheet next to it (`SiteNav.svelte` →
`SiteNav.css`). Each route imports its own `page.css`. Both consume tokens
from `root.css` and never redefine them.

### Where page logic lives

Behaviour that isn't markup goes in `src/scripts/`. The service-request form's
validation rules, payload shaping, and submit lifecycle live in
`scripts/pages/contact.js`; the component imports them and stays limited to
rendering and event binding.

`scripts/mowline.js` is a Svelte action powering the site's signature device —
a rule that sweeps across as its section enters the viewport, like a mower pass.
Apply it with `use:mowline` on any `.mowline` element. It disconnects its
observer after the first sweep and falls back to the finished state when
`IntersectionObserver` is unavailable.

`lib/safehtml.js` is a small allow-list sanitizer. Portal-managed copy (about
headers/sections) may contain a few formatting tags like `<br>`; these fields
are rendered with `{@html safehtml(...)}` so the whitelist is honored and
everything else is stripped.

---

## Data flow

All content comes from the centralized Celtech API. `lib/api/client.js` is the
single place that talks to it: it wraps `fetch`, maps the API DTOs into the
shapes the components expect, and exposes a small `api` object.

- **Gallery** — one payload of tags + images. Tags are either `category`
  (classifiers like *Hardscaping*, *Mowing*) or `album` (a specific job). Images
  reference tags by id, so an image can belong to many categories/albums. The
  client normalizes and sorts this once, then derives:
  - `api.gallery(opts)` — flat, sorted list of projects (supports
    `random`, `limit`, `tag`, `album`).
  - `api.albums(opts)` — album header cards (cover image + count), sorted by
    the portal-managed position.
  - `api.galleryGrouped(opts)` — `{ albums, categories, projects, loose }` in
    one shot for the gallery page.
- **Site meta** — `api.meta(opts)` returns the about-page copy (a header, three
  header/section blocks, a list of small header/blurb pairs, and an about image).
- **Inquiries** — `api.submitInquiry(payload)` POSTs the contact form to the
  centralized inquiry endpoint for this site.

The raw gallery and meta payloads are cached per module load and shared across
components so multiple readers don't refetch. `refreshGallery()` /
`refreshMeta()` clear those caches. In SvelteKit `load` functions the
request-scoped `fetch` is passed through and bypasses the cache to avoid
cross-request leakage on the server.

### Loading pattern

Pages that render server-side content use a `load` function:

- `routes/+page.js` → album headers for the home "Recent jobs" strip.
- `routes/gallery/+page.js` → the grouped gallery.
- `routes/bio/+page.js` → the site meta.

The gallery page keeps its interactive state (album drill-in, category filters,
lightbox, hash deep-links like `/gallery#album-<slug>`) client-side, but the
initial data arrives via `load` for first paint.

---

## Configuration

Copy `web/.env.example` to `web/.env` and set:

| Variable | Purpose |
|---|---|
| `PUBLIC_API_BASE` | Origin of the Celtech API. Empty in dev to use the Vite proxy; set to the API origin in production. |
| `PUBLIC_SITE_SLUG` | This site's slug on the Celtech platform (defaults to `ells`). |

Only `PUBLIC_`-prefixed variables reach the browser.

---

## Running locally

You need Node 20+.

```bash
cd web
npm install
npm run dev
```

Runs on `:5173`. In dev, Vite proxies `/api` so the browser sees a single
origin and CORS never comes up.

> **You must have the Celtech API running to see real content.** All data —
> gallery, albums, about-page copy, and images — now lives in the Celtech repo,
> so testing site content locally means running that API (and pointing
> `PUBLIC_API_BASE` at it, or relying on the Vite `/api` proxy). Without it, the
> gallery and about page fall back to their empty/placeholder states.

Useful scripts (run from `web/`):

```bash
npm run dev       # dev server
npm run build     # production build (Node server via adapter-node)
npm run preview   # preview the production build
npm run check     # svelte-check type/diagnostics pass
```

---

## Deploying

`npm run build` produces a Node server at `build/index.js` via
`@sveltejs/adapter-node`. The app is containerized (`web/Dockerfile`) and
deploys to DigitalOcean App Platform using `.do/app.yaml`.

Set the production environment variables (`PUBLIC_API_BASE`, `PUBLIC_SITE_SLUG`)
so the deployed site reads from the live Celtech API.

See **[DEPLOYMENT.md](DEPLOYMENT.md)** for the full walkthrough — app spec,
Dockerfile, health checks, domains, and troubleshooting.

Quick version:

```bash
# edit .do/app.yaml: set your GitHub repo and the CHANGE_ME values
doctl apps create --spec .do/app.yaml
```

---

## Testing

The service-request validation is pure and importable:

```js
import { validate, emptyForm } from '$scripts/pages/contact.js';
```

No test runner is wired up yet — add Vitest when you want the suite.
```