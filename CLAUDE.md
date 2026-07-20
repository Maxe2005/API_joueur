# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

This is `API_joueur`, the Spring Boot / Java 21 player-profile microservice of the Gatcha game (level, XP, monster inventory), checked out as a git submodule of the `GatchaApi` root repo — see the root repo's CLAUDE.md for the cross-service architecture, ports, and how the stack is run.

This service has **no standalone way to run itself** — no local `docker-compose.yml` exists here anymore (removed once the root orchestrator took over). It only runs via the root `docker-compose.yaml`, either through the root `make` targets or through this directory's own `Makefile`, which is a thin wrapper (`COMPOSE = docker compose -f ../docker-compose.yaml`, `SVC = api-joueur`) exposing `make up/down/down-v/reset-volumes/ps/logs/build/restart` scoped to this one service. All runtime config (Mongo host/port/db, `AUTH_API_HOST`/`AUTH_API_PORT`) is injected by the root compose's `environment:` block — `application.properties` only holds fallback defaults for running outside Docker.

## Commands

```bash
./mvnw clean package                              # build
./mvnw test                                       # run full test suite
./mvnw test -Dtest=ClassName                      # single test class
./mvnw test -Dtest=ClassName#methodName           # single test method
```

No linter/formatter is configured for this service (unlike `API_invocations`, which has Spotless/Checkstyle).

Runs on port 8082 (host) via the root compose; Swagger UI at `:8082/swagger-ui/index.html`.

## Architecture

Standard controller → service → repository layering, all under `com.imt.API_joueur`:
- `controller/PlayerController` — REST endpoints under `/api/players`, request/response DTOs declared inline as records (`XpRequest`, `MonsterRequest`, `CreatePlayerRequest`).
- `service/PlayerService` — leveling and inventory business logic.
- `repository/PlayerRepository` — Spring Data MongoDB repository (`players` collection).
- `model/Player` — the Mongo document (id, username (unique, indexed), level, experience, `monsterIds`).
- `config/AuthInterceptor` + `config/WebConfig` — auth enforcement (see below).
- `dto/auth/` — `TokenRequest`/`TokenResponse` shapes for calling the auth service; `TokenResponse` now carries both `user` and `role` (`USER`/`ADMIN`, mirroring `API_authentification`'s `Role` enum) and is `@JsonIgnoreProperties(ignoreUnknown = true)` for forward compatibility.

### Auth flow

`AuthInterceptor` is a `HandlerInterceptor` (not a security filter) registered in `WebConfig` on `/api/players/**`, excluding Swagger routes and `POST /api/players` itself (account creation must be reachable without a token — this is deliberate, not an oversight). For every other request it forwards the caller's bearer token to `API_authentification`'s `/user/verify-token` via `RestTemplate` and fails closed:
- missing token → 401
- auth service returns 4xx (invalid/expired token) → 401
- auth service returns 5xx → 401 (treated as invalid rather than propagating 500)
- auth service unreachable entirely → 500

**Important architectural point**: `API_invocations` forwards the *original caller's* bearer token when it calls this service (per the root repo's saga pattern), so the identity `AuthInterceptor` verifies here is always the actual player, never a service credential. Any ownership check added on top of token verification (path `{username}` must match the authenticated identity, unless role is `ADMIN`) is therefore consistent with that design, not a workaround for it.

### Leveling model (`PlayerService`)

- Max level is 50; XP threshold to go from level *n* to *n+1* is `50 * 1.1^(n-1)`, checked in a loop (`checkLevelUp`) so a single large XP grant can cascade multiple level-ups in one call.
- Inventory capacity is `10 + (level - 1)` monster slots.

## Git workflow (required)

For any piece of work beyond a trivial one-line fix: create a dedicated branch (`feat/...`, `fix/...`, `perf/...`) from the integration branch (`development` here), commit in atomic steps with conventional-commit messages in French (`feat:`/`fix:`/`perf:`/`docs:` plus a body explaining the why), then merge back with `--no-ff`. Never commit sizeable work directly on the integration branch.

