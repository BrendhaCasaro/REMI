# REMI — Agent Guide

## Project Overview

Digital media management system with three components:
- **`central/`** — Spring Boot 4.0.6 / Java 25 (orchestrator, auth, user & media APIs)
- **`node/`** — Spring Boot 4.0.6 / Java 25 (file storage, health, metrics APIs)
- **`ui/`** — React Router 7 + React 19 + Vite 8 + Tailwind CSS 4 (SSR frontend)

Infrastructure: PostgreSQL 16 (Docker Compose via `compose.yaml`).

---

## Build & Test Commands

### Java (central / node)

```bash
# Build all modules
./mvnw clean install

# Run all tests for a module
./mvnw test -pl central
./mvnw test -pl node

# Run a single test class
./mvnw test -pl central -Dtest=DigitalMediaApplicationTests
./mvnw test -pl node -Dtest=NodeDigitalMediaApplicationTests

# Run a single test method
./mvnw test -pl central -Dtest=DigitalMediaApplicationTests#contextLoads

# Build without tests
./mvnw clean install -DskipTests

# Run app locally (requires PostgreSQL on localhost:5432)
./mvnw spring-boot:run -pl central
./mvnw spring-boot:run -pl node
```

**Note:** Use `./mvnw` (Maven wrapper) from the module directory or root. Tests require a running PostgreSQL instance (see `compose.yaml` for credentials).

### UI

```bash
cd ui

# Install dependencies
npm install

# Development server with HMR
npm run dev

# Build for production
npm run build

# Type-check (typegen + tsc)
npm run typecheck

# Start production server
npm run start
```

### Infrastructure

```bash
# Start PostgreSQL
docker compose up -d
```

---

## Code Style — Java

### Imports
- No wildcard imports except in entities (`import jakarta.persistence.*`, `import lombok.*`) and controllers (`import org.springframework.web.bind.annotation.*`).
- Standard ordering: project → Jakarta → Lombok → Spring → Java stdlib → other.
- No blank-line groups between imports.

### Formatting
- 4-space indentation, K&R braces (opening brace on same line).
- DTOs are `record` types — single-line for few fields, multi-line (one param per line, 8-space indent) for more.
- Stream chains: each call on a new line (8-space indent).
- JPQL: Java text blocks (`"""..."""`).

### Types & Naming
- **Classes:** PascalCase, suffixed by role (`Controller`, `Service`, `Repository`, `Request`, `Response`, `Exception`, `Config`). Entities are plain nouns (`User`, `Node`, `Media`).
- **Methods:** camelCase, verb-prefixed (`createUser`, `getAllUsers`, `deleteNode`, `uploadMedia`).
- **Fields/Variables:** camelCase, no prefixes. Injected deps are `private final`. Entity fields are `private` (often via Lombok `@Getter`/`@Setter`).
- **Enums:** PascalCase, uppercase constants (`ADMIN`, `USER`; `ONLINE`, `OFFLINE`).

### Annotations
- `@Service`, `@RestController`, `@Configuration`, `@Component` for stereotypes.
- `@RequestMapping` on class + `@GetMapping`/`@PostMapping`/etc. on methods.
- `@RequiredArgsConstructor` (Lombok) for constructor injection in **central** module.
- `@Transactional` from `jakarta.transaction.Transactional` on mutating service methods.
- `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@Enumerated(EnumType.STRING)`, `@ManyToOne`, `@JoinColumn` for persistence.
- `@RestControllerAdvice` + `@ExceptionHandler` for global error handling.
- `@DynamicUpdate` on entities (Hibernate).
- `@SpringBootTest` + `@Test` for tests.

### Lombok
- **Central module uses it heavily:** `@Getter`, `@Setter`, `@RequiredArgsConstructor`, `@NoArgsConstructor`, `@AllArgsConstructor`.
- **Node module does NOT use Lombok** — write explicit constructors, getters, and setters.

### Error Handling
- Unchecked (runtime) exceptions only. No checked exceptions in signatures.
- `orElseThrow` with `EntityNotFoundException` for missing entities.
- Wrapping `IOException` → `UncheckedIOException`.
- Custom exceptions extend `RuntimeException` (e.g., `OrchestratorException`).
- Global handler per module (`GlobalHandlerException` with `@RestControllerAdvice`). Returns `ResponseEntity<ErrorResponse>`.
- Silent catch only when intentional (e.g., filtering unhealthy nodes: `catch (Exception _) { }`).

### Testing
- JUnit 5 + Spring Boot test slices (`@SpringBootTest`).
- Test class naming: `XxxApplicationTests` or `XxxTest`.
- One test class per file, test methods annotated with `@Test`.

---

## Code Style — TypeScript / React

### Imports
- `import type { ... }` for type-only imports (`verbatimModuleSyntax` is on).
- Groups separated by blank lines: (1) external packages, (2) local types / CSS side-effects, (3) local assets.
- Relative paths only (no `~/*` alias used yet, even though configured).
- Assets imported as default: `import logo from "./logo.svg"`.

### Formatting
- 2-space indentation, semicolons required, trailing commas on multi-line constructs.
- Double quotes in JSX attributes, single quotes for TypeScript strings (Prettier default).

### Components
- Function declarations only — no `React.FC`, no arrow-function components.
- Route pages: `export default function PageName()`.
- Shared components: `export function ComponentName()` — named exports.
- Route module exports: `export function meta()`, `export const links`.
- Props typed inline for simple cases, or via auto-generated `Route` types from React Router (`./+types/*`).
- No React hooks in presentational components.

### Types & Naming
- **Files:** kebab-case for config/root (`vite.config.ts`, `react-router.config.ts`), camelCase for components (`home.tsx`, `welcome.tsx`).
- **Components:** PascalCase.
- **Functions/Variables:** camelCase. Use `let` for mutable variables, `const` for constants.
- **CSS:** Tailwind utility classes in `className`. No CSS modules or styled-components.
- Config objects use `satisfies` keyword for type-safe literals (`satisfies Config`).

### Routing
- Routes declared in `app/routes.ts` using `@react-router/dev/routes` helpers.
- Route component types auto-imported from `./+types/<route_name>`.
- SSR enabled, all v8 future flags on in `react-router.config.ts`.

### Conventions
- `className` not `class`.
- Self-closing tags for components without children (`<Meta />`, `<Links />`).
- `&apos;` for apostrophes in JSX text.
- `import.meta.env.DEV` for dev-only code.
