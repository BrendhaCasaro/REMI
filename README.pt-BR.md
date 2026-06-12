> **English?** 🇺🇸 Read the [English README](README.md).

# REMI — Repositório de Escritório de Mídias Inteligente

Sistema distribuído de gerenciamento de mídias digitais. Composto por um **orquestrador central** que gerencia metadados, autenticação e a escolha do melhor nó de armazenamento, **múltiplos nós de armazenamento** que persistem os arquivos em disco, e uma **interface web** com renderização server-side (SSR).

## Segurança entre Central e Nodes

Cada nó de armazenamento exige uma chave secreta definida via variável de ambiente (`NODE_AUTH_KEY`). A central envia essa chave como token Bearer no header `Authorization` em todas as requisições aos nós. Apenas requisições que contenham a chave correta são aceitas — cada nó pode ter sua própria chave, permitindo isolamento entre eles.

## Arquitetura

```
  UI (React SSR)
        │
        │ REST API (JWT Bearer)
        ▼
  Central API (Spring Boot)
        │
        │ Orchestrator seleciona nó ONLINE com mais disco livre
        │ RestClient faz proxy das operações de arquivo
        ▼
  Node 1 … N (Spring Boot)
        │
        │ Arquivos salvos em disco (/storage)
        ▼
  PostgreSQL (single database ou um por app)
```

## Módulos

| Módulo | Tecnologias | Função |
|---|---|---|
| `central/` | Spring Boot 4.0.6, Java 25, Spring Data JPA, Spring Security, JWT | Auth (login JWT), CRUD de usuários, CRUD de nós de armazenamento, CRUD de metadados de mídia, orquestrador de upload/download/delete |
| `node/` | Spring Boot 4.0.6, Java 25, Spring Data JPA | Upload, download e delete de arquivos; health check (`/api/health`); métricas de disco (`/api/metrics`) |
| `ui/` | React 19, React Router 7 (SSR), Vite 8, Tailwind CSS 4, TypeScript, shadcn/ui | Interface web com tabelas dinâmicas, i18n (pt-BR e en), notificações toast |

## Imagens Docker Hub

As imagens são publicadas automaticamente via GitHub Actions em:

[https://hub.docker.com/u/brendhacasaro](https://hub.docker.com/u/brendhacasaro)

- `brendhacasaro/remi-central:latest`
- `brendhacasaro/remi-node:latest`
- `brendhacasaro/remi-ui:latest`

Plataformas disponíveis: `linux/amd64` e `linux/arm64`.

## Dockerfiles

Cada módulo possui seu próprio Dockerfile com multi-stage build:

- **`central/Dockerfile`** — Estágio 1: build com `maven:3.9.11-eclipse-temurin-25`. Estágio 2: runtime com `eclipse-temurin:25-jre`. Expoe a porta `8080`.
- **`node/Dockerfile`** — Mesma estratégia do central. Expoe a porta `8080`.
- **`ui/Dockerfile`** — Três estágios: dependências de desenvolvimento, dependências de produção e build com `node:20-alpine`. Estágio final copia apenas `node_modules` de produção e o build. Inicia com `npm run start`.

## Variáveis de Ambiente

### Central (imagem `brendhacasaro/remi-central`)

| Variável | Obrigatória | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Sim | JDBC URL do banco PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Sim | Usuário do banco de dados |
| `SPRING_DATASOURCE_PASSWORD` | Sim | Senha do banco de dados |
| `APP_ADMIN_PASSWORD` | Sim | Senha da conta admin padrão criada automaticamente para uso inicial |

### Node (imagem `brendhacasaro/remi-node`)

| Variável | Obrigatória | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | Sim | JDBC URL do banco PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | Sim | Usuário do banco de dados |
| `SPRING_DATASOURCE_PASSWORD` | Sim | Senha do banco de dados |
| `NODE_AUTH_KEY` | Sim | Chave secreta usada para autenticar requisições vindas da central. A central envia esta chave como Bearer token no header `Authorization`. Sem ela o nó rejeita a chamada |

## Desenvolvimento local (sem Docker)

### Pré-requisitos

- Java 25 (definido via `mise.toml` ou instalado manualmente)
- Node.js 20+
- PostgreSQL rodando localmente

### Banco de dados

Crie os databases necessários no PostgreSQL local. Você pode usar um único banco compartilhado entre central e todos os nós, ou um banco separado para cada aplicação — as variáveis `SPRING_DATASOURCE_URL` em cada `application.properties` definem essa configuração.

### Backend (central e node)

Cada módulo possui seu próprio Maven wrapper:

```bash
cd central && ./mvnw spring-boot:run
cd node   && ./mvnw spring-boot:run
```

Ou pela raiz com Maven instalado:

```bash
mvn test -pl central
mvn test -pl node
mvn clean install
```

### Frontend

```bash
cd ui
npm install
npm run dev       # desenvolvimento com HMR
npm run typecheck # typecheck (react-router typegen + tsc)
npm run build     # build de produção
npm run start     # servidor SSR de produção
```

O frontend possui um modo mock (`VITE_USE_MOCK=true` / `USE_MOCK = true` em `ui/app/lib/api.ts`) que simula as APIs sem necessidade do backend rodando.
