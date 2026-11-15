# AI Personal Lifestyle Coach — Web Frontend

A React (Vite) UI for the same coach the console app (`com.lifestylecoach.Main`) uses, talking to it over a small HTTP API (`com.lifestylecoach.web.WebServer`).

## Development

Two processes, so the frontend hot-reloads while you edit it:

```powershell
# 1. Backend API (from the project root) -- compiles then starts on :8080
./run-backend.ps1

# 2. Frontend dev server (from frontend/)
npm install
npm run dev   # listens on :5173, proxies /api/* to :8080 (see vite.config.js)
```

Open http://localhost:5173. If you see `ECONNREFUSED` proxy errors in the
`npm run dev` output, the backend isn't running -- start `run-backend.ps1`
first (or check its window for a startup error, e.g. a missing `.env` key).

## Production

Build the frontend, then package the backend + frontend + all dependencies into
one runnable jar with Maven (this dev machine doesn't have Maven installed --
use a machine/CI environment that does, or the portable-binary approach: download
the Maven zip from https://maven.apache.org/download.cgi, unzip it, and call
`<unzip-dir>/bin/mvn` directly without installing anything system-wide):

```bash
cd frontend
npm run build            # outputs frontend/dist, bundled into the jar below
cd ..
mvn clean package         # outputs target/personal-lifestyle-coach.jar
java -jar target/personal-lifestyle-coach.jar
```

`WebServer` serves the built frontend directly from `frontend/dist` (bundled
into the jar at build time) alongside the `/api/*` routes, so there's just one
process and one port (`:8080` by default, override with `WEB_PORT`). Set
`GEMINI_API_KEY` and `USDA_API_KEY` as real environment variables in production
rather than shipping a `.env` file.

`./run-backend.ps1` (recompiling straight from source with `javac` against the
local `.m2` cache) remains just a local dev convenience for this machine since
it lacks Maven day-to-day -- the Maven build above is the one to use for an
actual deployment.

## API

- `GET /api/health`
- `GET /api/profile/{name}` — 404 if no saved profile
- `POST /api/profile` — body matches the `UserProfile` JSON shape; returns `{ profile, created, analysis }` (`analysis` is only populated the first time a profile is created)
- `POST /api/chat` — body `{ name, message }`; returns `{ reply, nutrition }`
