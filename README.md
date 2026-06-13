> **Português?** 🇧🇷 Leia o [README em Português](README.pt-BR.md).

# REMI — Distributed digital media manager

Distributed digital media management system. Composed of a **central orchestrator** that manages metadata, authentication, and selection of the best storage node, **multiple storage nodes** that persist files to disk, and a **web interface** with server-side rendering (SSR).

## Security between Central and Nodes

Each storage node requires a secret key defined via environment variable (`NODE_AUTH_KEY`). The central sends this key as a Bearer token in the `Authorization` header on all requests to nodes. Only requests containing the correct key are accepted — each node can have its own key, allowing isolation between them.

## Architecture

```
  UI (React SSR)
        │
        │ REST API (JWT Bearer)
        ▼
  Central API (Spring Boot)
        │
        │ Orchestrator selects ONLINE node with most free disk
        │ RestClient proxies file operations
        ▼
  Node 1 … N (Spring Boot)
        │
        │ Files saved to disk (/storage)
        ▼
  PostgreSQL (single database or one per app)
```

## Modules

| Module | Technologies | Role |
|---|---|---|
| `central/` | Spring Boot 4.0.6, Java 25, Spring Data JPA, Spring Security, JWT | Auth (JWT login), user CRUD, storage node CRUD, media metadata CRUD, upload/download/delete orchestrator |
| `node/` | Spring Boot 4.0.6, Java 25, Spring Data JPA | File upload, download, and delete; health check (`/api/health`); disk metrics (`/api/metrics`) |
| `ui/` | React 19, React Router 7 (SSR), Vite 8, Tailwind CSS 4, TypeScript, shadcn/ui | Web interface with dynamic tables, i18n (pt-BR and en), toast notifications |

## Docker Hub Images

Images are automatically published via GitHub Actions at:

[https://hub.docker.com/u/brendhacasaro](https://hub.docker.com/u/brendhacasaro)

- `brendhacasaro/remi-central:latest`
- `brendhacasaro/remi-node:latest`
- `brendhacasaro/remi-ui:latest`

Available platforms: `linux/amd64` and `linux/arm64`.

## Dockerfiles

Each module has its own multi-stage build Dockerfile:

- **`central/Dockerfile`** — Stage 1: build with `maven:3.9.11-eclipse-temurin-25`. Stage 2: runtime with `eclipse-temurin:25-jre`. Exposes port `8080`.
- **`node/Dockerfile`** — Same strategy as central. Exposes port `8080`.
- **`ui/Dockerfile`** — Three stages: dev dependencies, production dependencies, and build with `node:20-alpine`. Final stage copies only production `node_modules` and the build. Starts with `npm run start`.

## Environment Variables

### Central (`brendhacasaro/remi-central` image)

| Variable | Required | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password |
| `APP_ADMIN_PASSWORD` | Yes | Password for the default admin account created automatically for initial use |
| `JWT_SECRET` | No (default `your-jwt-hash` in properties) | Secret key used to sign JWT tokens. Can be set as environment variable or via `jwt.secret` in `application.properties`. **Must be changed** to a strong value in production |
| `JWT_EXPIRATION-MS` | No (default `864000000` in properties) | JWT token expiration in milliseconds. Can be set as environment variable or via `jwt.expiration-ms` in `application.properties` |
| `SPRING_SERVLET_MULTIPART_MAXFILESIZE` | No (default `100MB` in properties) | Maximum file size per upload. Can be set as environment variable or via `spring.servlet.multipart.max-file-size` in `application.properties` |
| `SPRING_SERVLET_MULTIPART_MAXREQUESTSIZE` | No (default `150MB` in properties) | Maximum total multipart request size. Can be set as environment variable or via `spring.servlet.multipart.max-request-size` in `application.properties` |

### Node (`brendhacasaro/remi-node` image)

| Variable | Required | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Yes | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Yes | Database password |
| `NODE_AUTH_KEY` | Yes | Secret key used to authenticate requests coming from central. Central sends this key as a Bearer token in the `Authorization` header. Without it the node rejects the call |
| `SPRING_SERVLET_MULTIPART_MAXFILESIZE` | No (default `100MB` in properties) | Maximum file size per upload. Can be set as environment variable or via `spring.servlet.multipart.max-file-size` in `application.properties` |
| `SPRING_SERVLET_MULTIPART_MAXREQUESTSIZE` | No (default `150MB` in properties) | Maximum total multipart request size. Can be set as environment variable or via `spring.servlet.multipart.max-request-size` in `application.properties` |

## Local development (without Docker)

### Prerequisites

- Java 25 (set via `mise.toml` or installed manually)
- Node.js 20+
- PostgreSQL running locally

### Database

Create the required databases in local PostgreSQL. You can use a single shared database between central and all nodes, or a separate database for each application — the `SPRING_DATASOURCE_URL` variables in each `application.properties` define this configuration.

### Backend (central and node)

Each module has its own Maven wrapper:

```bash
cd central && ./mvnw spring-boot:run
cd node   && ./mvnw spring-boot:run
```

Or from the root with Maven installed:

```bash
mvn test -pl central
mvn test -pl node
mvn clean install
```

### Frontend

```bash
cd ui
npm install
npm run dev       # development with HMR
npm run typecheck # typecheck (react-router typegen + tsc)
npm run build     # production build
npm run start     # production SSR server
```

The frontend has a mock mode (`VITE_USE_MOCK=true` / `USE_MOCK = true` in `ui/app/lib/api.ts`) that simulates the APIs without needing the backend running.
