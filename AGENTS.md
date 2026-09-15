# Crypts & Dungeons — Base44 Setup

## Project Type
The original repo is a native Android app (Kotlin + Jetpack Compose) — a tactical RPG dungeon crawler.
Base44 recreates the game as a **web app** (vanilla HTML/CSS/JS) so it runs interactively in the preview.

## How It Works
1. `Dockerfile.base44` — Python 3.12 slim image serving static files
2. `docker-compose.base44.yml` — builds and serves on port 3000
3. `web/index.html`, `web/style.css`, `web/game.js` — the playable web game

## Build & Run
```bash
docker compose -f docker-compose.base44.yml up -d --build
```

## After Code Changes
The Docker image bakes the web files. After editing `web/` files:
```bash
docker compose -f docker-compose.base44.yml up -d --build
```
Then use `reload_preview` to refresh.

## Original Android App
The original Kotlin/Compose source remains in `app/`. Key files:
- `app/src/main/java/com/example/` — all Android source
- `viewmodel/GameViewModel.kt` — game state + logic (translated to web/game.js)
- `data/GameDatabase.kt` — enemies, quests, loot (translated to web/game.js)
- `engine/CombatEngine.kt` — D20 combat (translated to web/game.js)
- `ui/screens/` — 9 Compose screens (recreated as web screens)
