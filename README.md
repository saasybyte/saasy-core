# saasy-core

Authentication service for [SaasyByte](https://github.com/saasybyte/saasybyte), an open-source real-time AI voice platform.

Core owns the trust boundary: invite code lifecycle (generation, validation, claiming), ES256 JWT issuance, and per-code usage budgets. An invite code carries a time-windowed budget of session seconds; Core tracks consumption reported by the signaling server and tells it when to warn or terminate. Consumers validate JWTs locally with Core's public key; Core only mints them.

## How It Fits

- **Serves saasy-web** (REST): invite code validation and JWT issuance.
- **Serves saasy-signal** (gRPC): usage/budget tracking during active sessions.
- **Serves admins** (REST): invite code generation and claiming via an `X-Admin-Key` header.
- **Proto types** from the [saasy-proto](https://github.com/saasybyte/saasy-proto) git submodule; REST contracts generated from `api/openapi.yaml`.

See the [platform overview](https://github.com/saasybyte/saasybyte) for the full architecture.

## Build & Run

Requirements: JDK 25, PostgreSQL. Flyway migrations run automatically on startup.

```bash
git submodule update --init   # saasy-proto
make run          # run dev server (Spring local profile)
make build        # build without tests
make generate     # regenerate protobuf, OpenAPI server interfaces, Bruno collection
```

Configuration comes from environment variables or a gitignored `application-local.yml` (Spring `local` profile); see `.env.example` for the required variables (database, `JWT_PRIVATE_KEY`, `ADMIN_API_KEY`, CORS origins). Note that Spring does not auto-load a `.env` file: export the variables (`set -a && source .env && set +a`) or use `application-local.yml`. Ports: 8082 (REST), 9092 (gRPC).

Protobuf and OpenAPI codegen run inside Gradle (no extra tools needed); regenerating the Bruno collection (`make bruno`) additionally requires the Bruno CLI (`bru`).

A `Dockerfile` is included; `docker build .` needs no credentials.

## License

Apache-2.0, see [LICENSE](LICENSE).
