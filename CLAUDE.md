# saasy-core

## Commands
- `make run` — run dev server (Spring local profile)
- `make clean-run` — clean + run
- `make build` / `make clean-build` — build / clean + build (tests skipped)
- `make generate` — full codegen: protobuf + OpenAPI + Bruno
- `make proto` — regenerate protobuf/gRPC stubs
- `make api` — regenerate OpenAPI server interfaces
- `make bruno` — regenerate Bruno collection from OpenAPI

## Conventions
- **Database**: jOOQ with raw `DSL.field()` / `DSL.table()` references and manual `Record` mapping. No ORM, no jOOQ codegen.
- **REST contracts**: OpenAPI Generator produces controller interfaces (e.g., `InviteCodesApi`). Controllers implement these interfaces — do not define endpoints manually.
- **OpenAPI summaries**: must be filename-friendly (lowercase, hyphens, no spaces). Bruno generates `.bru` filenames from them (e.g., `summary: generate-invite-codes` → `generate-invite-codes.bru`).
- **Proto types**: from `saasy-proto` submodule (e.g., `saasy.core.v1.Core`). Do not define proto types locally.
- **Config**: `@ConfigurationProperties` data classes with `@ConfigurationPropertiesScan`. Nested structure mirrors YAML keys.
- **Validation results**: sealed class per domain operation (e.g., `ValidationResult`). Controllers map each variant to an HTTP status.
- **gRPC errors**: throw `io.grpc.StatusException` with appropriate gRPC status codes.
- **REST errors**: return `ResponseEntity` with HTTP status codes directly. No global exception handler.
- **Auth**: `X-Admin-Key` header for admin endpoints. Public endpoints (e.g., `/validate`) have no auth. No JWT validation — this service only mints JWTs.
- **Secrets**: provided via environment variables or `application-local.yml` (gitignored). Never hardcode or commit secrets.
- **Tests**: none yet.

## Service Boundaries
- **Serves saasy-signal** (gRPC): usage/budget tracking during active sessions.
- **Serves web client** (REST): invite code validation and JWT issuance.
- **Serves admins** (REST): invite code generation and claiming via `X-Admin-Key`.
- **Proto schema from saasy-proto** (submodule): do not define proto types locally.
- **Does not own**: JWT validation (consumers validate with the public key), media (saasy-sfu), signaling (saasy-signal), AI inference (saasy-orchestrator), proto schema (saasy-proto).
