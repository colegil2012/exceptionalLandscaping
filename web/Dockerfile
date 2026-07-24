# =============================================================================
# Multi-stage Dockerfile — landscaping-web (SvelteKit, adapter-node)
# Build context is the repo root; this file lives at web/Dockerfile.
# =============================================================================

# ---- build stage ------------------------------------------------------------
FROM node:22-alpine AS build
WORKDIR /build

# Lockfile-first so the install layer caches independently of source changes.
COPY web/package.json web/package-lock.json* ./
RUN npm ci

COPY web/ ./

# No build-time API URL needed: client.js reads PUBLIC_API_BASE through
# $env/dynamic/public, so the value is resolved at run time from the
# container environment. Changing it is an env edit + restart, not a rebuild.
RUN npm run build

# Drop dev dependencies before copying into the runtime image.
RUN npm prune --omit=dev

# ---- runtime stage ----------------------------------------------------------
FROM node:22-alpine AS runtime
WORKDIR /app

RUN apk add --no-cache curl

# node:alpine ships a non-root `node` user already; use it rather than
# creating another.
COPY --from=build --chown=node:node /build/build ./build
COPY --from=build --chown=node:node /build/node_modules ./node_modules
COPY --from=build --chown=node:node /build/package.json ./package.json

USER node

EXPOSE 3000

ENV NODE_ENV=production \
    PORT=3000 \
    HOST=0.0.0.0

# Exec form so SIGTERM reaches node directly and in-flight requests drain.
ENTRYPOINT ["node", "build/index.js"]

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/ || exit 1
