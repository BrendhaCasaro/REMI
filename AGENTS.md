# REMI Agent Notes

## Shape
- `central/`: Spring Boot 4.0.6 / Java 25 API orchestrator for auth, users, nodes, and media metadata.
- `node/`: Spring Boot 4.0.6 / Java 25 storage node API for file upload/download/delete, health, and metrics.
- `ui/`: React Router 7 SSR app on React 19, Vite 8, Tailwind CSS 4, shadcn-style components.

## Commands
- Root `pom.xml` only aggregates `central` and `node`; there is no root `./mvnw`.
- From repo root with system Maven: `mvn test -pl central`, `mvn test -pl node`, `mvn clean install`.
- From a module with wrapper: `cd central && ./mvnw test`, `cd node && ./mvnw test`.
- Single Java test class examples: `mvn test -pl central -Dtest=DigitalMediaApplicationTests`, `mvn test -pl node -Dtest=NodeDigitalStoredMediaApplicationTests`.
- UI commands from `ui/`: `npm install`, `npm run dev`, `npm run build`, `npm run typecheck`, `npm run start`.
- `npm run typecheck` runs `react-router typegen && tsc`; generated `.react-router/` is ignored.

## Local Services
- No `compose.yaml` is present in this checkout.
- Java apps and `@SpringBootTest` context tests expect PostgreSQL on localhost.
- Central DB: `jdbc:postgresql://localhost:5432/remi-centraldb`, user `myuser`, password `secret`.
- Node DB: `jdbc:postgresql://localhost:5432/remi-nodedb`, user `myuser`, password `secret`.

## Runtime Gotchas
- Central stores media metadata, chooses a node via `/api/health` and `/api/metrics`, then calls node `/api/files/upload`.
- Node persists file paths in Postgres and writes actual files under absolute `/storage`; local runs need that path writable.
- UI API calls are currently mocked by `const USE_MOCK = true` in `ui/app/lib/api.ts`; changing to real API also exposes hardcoded `API_BASE = "http://localhost:8080"`.
- Central security permits `/api/login`; most other central endpoints require JWT roles.

## UI Notes
- Routes are declared in `ui/app/routes.ts`; root SSR shell is `ui/app/root.tsx`.
- TS path aliases are configured and used: `~/*` and `@/*` map to `ui/app/*`.
- shadcn aliases are in `ui/components.json`; shared UI components live under `ui/app/components/ui`.
- i18n resources live in `ui/app/i18n/locales/{pt-BR,en}`; fallback language is `pt-BR`.

## Java Notes
- Both Java modules have Lombok configured; do not assume node is Lombok-free.
- Controllers often use `org.springframework.web.bind.annotation.*`; keep existing style when editing nearby code.
- Mutating service methods use `jakarta.transaction.Transactional`.
