# Deploying to DigitalOcean

Both components run as Docker containers in a **single App Platform app**.
This matches the cgsWeb/cgsKitchen pattern (App Platform, deploy-from-repo,
multistage Dockerfile, env vars per component with secrets encrypted), with
one structural difference worth understanding up front.

## Why one app with two components

cgsWeb is a single Spring Boot deployable serving its own Thymeleaf pages —
one component. This site is split: a JSON API and a separate SvelteKit server.

Putting both in one app means they share a hostname, and that has a
consequence that removes an entire category of bugs: **the browser only ever
talks to one origin.** `PUBLIC_API_BASE` stays empty, requests go to relative
`/api/...` paths, and CORS never engages because nothing is cross-origin.

Two separate apps would work too, but then you're managing two hostnames, two
certificates, a real CORS configuration, and a preflight round-trip on every
request. Only do that if the API genuinely needs its own hostname.

Cost is also lower: two components in one app on the basic tier runs roughly
$12–17/mo (`apps-s-1vcpu-1gb` for the API, `apps-s-1vcpu-0.5gb` for web),
versus paying an app minimum twice.

---

## What's in the repo

```
.do/app.yaml          App Platform spec — both components, ingress, env vars
api/Dockerfile        Maven build → JRE-alpine runtime, non-root
web/Dockerfile        npm build → node-alpine runtime, non-root
docker-compose.yml    Local dev: Mongo + API + web together
.dockerignore         Keeps node_modules/target out of the build context
```

Note both Dockerfiles use `source_dir: /` (the repo root) as build context and
reference their files as `api/...` / `web/...`. That's deliberate — it lets the
API Dockerfile copy `api/pom.xml` before `api/src` for dependency-layer
caching.

---

## Prerequisites

1. Repo pushed to GitHub, App Platform granted access
2. A Mongo instance — Atlas free tier or DO Managed MongoDB
3. A Space created, with its CDN endpoint enabled
4. `doctl` installed and authenticated (`doctl auth init`)

---

## First deploy

### 1. Point the spec at your repo

Edit `.do/app.yaml` and replace `YOUR_GH_USER/landscaping-site` in **both**
components with your actual `owner/repo`.

### 2. Fill in the values marked CHANGE_ME

Everything marked `CHANGE_ME` needs a real value. The ones marked
`type: SECRET` are encrypted by App Platform on first submission:

| Variable | Where it comes from |
|---|---|
| `MONGO_URI` | Atlas or DO Managed connection string |
| `SPACES_BUCKET` | Your Space name |
| `SPACES_CDN_BASE` | `https://<space>.<region>.cdn.digitaloceanspaces.com` |
| `SPACES_KEY` / `SPACES_SECRET` | API → Spaces Keys |
| `MAIL_*` | Your SMTP provider |

You can leave the secrets as `CHANGE_ME` in the file and set the real values
in the control panel instead — that keeps credentials out of git entirely,
which is the better habit. If you do put them in the spec, note that App
Platform returns them as `EV[1:...]` ciphertext on subsequent reads; paste
that ciphertext back rather than the plaintext when updating.

### 3. Create the app

```bash
doctl apps create --spec .do/app.yaml
```

Watch the build:

```bash
doctl apps list
doctl apps logs <APP_ID> --type build --follow
```

First build is slow — Maven downloads its whole dependency tree. Subsequent
builds hit the cached layer and are much faster, as long as `pom.xml` hasn't
changed.

### 4. Verify before touching DNS

```bash
APP=https://landscaping-site-xxxxx.ondigitalocean.app

curl -s $APP/actuator/health          # {"status":"UP"}
curl -s $APP/api/gallery | head -c 200 # JSON array
curl -s -o /dev/null -w '%{http_code}\n' $APP/  # 200
```

If `/api/gallery` 404s but `/` works, the ingress `preserve_path_prefix` is
wrong — see troubleshooting below.

### 5. Add the domain

Uncomment the `domains:` block in the spec, or add it in the control panel,
then `doctl apps update <APP_ID> --spec .do/app.yaml`.

**On Cloudflare, sequence matters.** Add the CNAME **DNS-only (grey cloud)**
first. Wait for App Platform to show the certificate as issued. *Then* switch
to proxied (orange) with SSL mode **Full (strict)**. Going orange before the
cert exists gives you confusing 5xx errors that look like app failures.

---

## Subsequent deploys

`deploy_on_push: true` is set on both components, so a push to `main` rebuilds
and redeploys automatically.

A caveat specific to two components in one repo: **both rebuild on every
push**, even a CSS-only change. App Platform doesn't do per-path change
detection from a single repo. Usually fine; if API builds get slow enough to
annoy, split into two repos or two apps.

To force a rebuild without a commit:

```bash
doctl apps create-deployment <APP_ID> --force-rebuild
```

---

## Health checks

The API exposes Spring Boot's split probes, configured in `application.yml`:

- `/actuator/health/readiness` — is it ready for traffic? Fails during
  startup, so App Platform holds the container out of rotation until Mongo
  connects. Tight thresholds.
- `/actuator/health/liveness` — is the JVM alive? Failure restarts the
  container. Deliberately more forgiving, so a long GC pause doesn't trigger
  a restart loop.

Only `health` is exposed and `show-details: never` is set — no environment
details, bean lists, or config leak from a public endpoint.

The web component just health-checks `/`, since a SvelteKit server that can
render the homepage is working.

---

## Local development with Docker

```bash
docker compose up --build
```

Mongo, API, and web come up together; web on `:3000`, API on `:8080`. The API
runs the `local` profile, so it seeds sample data on first start and reads
images from `./local-images` rather than Spaces.

One thing that differs from production here: compose sets
`PUBLIC_API_BASE=http://localhost:8080` because the two containers are on
separate ports, so the browser *is* making a cross-origin request. That's why
`CORS_ORIGINS=http://localhost:3000` is also set. In production both sit
behind one hostname and neither is needed.

For frontend work, `npm run dev` in `./web` is better — compose has no hot
reload.

---

## Troubleshooting

**`/api/*` returns 404 but the site loads.** The ingress rule lost
`preserve_path_prefix: true`. Without it App Platform strips the matched
prefix, so `/api/gallery` reaches Spring as `/gallery`, which has no mapping.

**Health check fails, container restarts forever.** Almost always Mongo. The
readiness probe includes a Mongo health indicator, so a bad `MONGO_URI` or a
firewall blocking App Platform's egress shows up as a failing health check
rather than an obvious connection error. Check `doctl apps logs <APP_ID>
--type run` for the actual exception. If you're on Atlas, confirm network
access allows App Platform (`0.0.0.0/0` while testing, then narrow it).

**Images 404 in production.** `SPACES_CDN_BASE` is wrong or the Space isn't
public. The API returns URLs built from that value without verifying they
resolve — `curl` one of the URLs from `/api/gallery` directly to see what the
CDN actually says.

**Build fails on `npm ci`.** No `package-lock.json` committed. The Dockerfile
copies `package-lock.json*` (optional glob) so the build won't fail on the
COPY, but `npm ci` requires the lockfile. Commit it.

**Contact form works locally, fails in production.** Mail. `MAIL_ENABLED` is
`false` by default, and the service logs-and-swallows send failures by design
so a dead SMTP server never costs you a lead. The inquiry is still in Mongo —
check the `inquiry` collection, then check run logs for the mail exception.

---

## Scaling

`instance_count: 1` is right to start. If you raise it, this app is safe to
run multi-instance: the API is stateless (no HTTP sessions — unlike cgsWeb,
which needed Spring Session for exactly this reason), and the SvelteKit server
holds no per-user state.

For automatic scaling, replace `instance_count` with:

```yaml
autoscaling:
  min_instance_count: 1
  max_instance_count: 3
  metrics:
    requests_per_second:
      per_instance: 100
```

CPU-based autoscaling needs a dedicated-CPU plan; request-based works on the
basic tier.
