# My Pokedex

Este repositório contém "My Pokedex", uma aplicação de exemplo construída sobre a plataforma JVM com Micronaut. O
objetivo deste projeto é servir como referência para aprendizado, experiments e como base para serviços que expõem APIs
relacionadas a Pokémons.

Resumo

- Propósito: fornecer uma API e conjunto de componentes para gerenciar e consultar dados de Pokémons, demonstrando boas
  práticas (TDD, arquitetura hexagonal, contratos).
- Público-alvo: desenvolvedores e contribuidores que desejam rodar, testar e estender o projeto.

Pré-requisitos

- Java 17+ (OpenJDK/Temurin recomendado) — o projeto é compatível com JVM 17+.
- Git
- Docker (opcional, requerido para alguns testes de integração que usam Testcontainers)

Clonar o repositório

1. git clone <repo-url>
2. cd my-pokedex

Build & execução

Usando o Gradle wrapper (recomendado):

1. ./gradlew clean build
2. ./gradlew run

Observações:

- Em Windows use `gradlew.bat` em vez de `./gradlew`.
- O build gera artefatos em `build/libs` e documentação OpenAPI em `build/classes/java/main/META-INF/swagger/`.

Testes

- Testes unitários e de integração são executados com:

  ./gradlew test

- Nota: alguns testes de integração usam Testcontainers e Docker deve estar disponível no host.

Estrutura do repositório (visão geral)

- `src/` — código fonte principal
- `specs/` — especificações de features, planos, contratos e tarefas
- `build.gradle.kts` — configuração do Gradle
- `application.yml` — configurações de runtime (em `src/main/resources`)
- `build/` — artefatos gerados e relatórios de teste

Arquitetura e boas práticas

- Arquitetura: o projeto segue princípios de Hexagonal / Clean Architecture — separação entre domínio, portas/adapters e
  infraestrutura.
- Testes: Test-First (TDD) obrigatório para novas features; cada feature inclui especificação em `specs/` e testes que
  devem falhar antes da implementação.
- Contratos: APIs públicas devem incluir contratos OpenAPI em `specs/*/contracts/`.

Como contribuir

1. Crie uma branch seguindo o padrão `###-feature-name` (ex.: `002-documentação-do-projeto`).
2. Adicione uma especificação em `specs/` (use os templates em `.specify/templates`).
3. Conteúdo do PR:
    - Testes que falhem inicialmente demonstrando o comportamento desejado (unit/contract/integration).
    - Spec atualizada em `specs/` e `plan.md` se aplicável.
    - Nota de migração se houver quebra de contrato.
4. Abra PR e aguarde revisão. PRs obrigatoriamente devem passar CI (linters, unit & contract tests).

Troubleshooting (comum)

- Erro: Docker não disponível para testes com Testcontainers
    - Solução: Instalar e configurar Docker Desktop ou engine; verificar que seu usuário tem permissão para acessar o
      socket Docker.
- Erro: Java incompatível
    - Solução: Instalar Java 17+ ou usar `./gradlew` que baixa runtime adequado em alguns setups.
- Build lento / problemas de rede ao baixar dependências
    ```markdown
    # My Pokedex

    My Pokedex is a sample JVM application built with Micronaut. It aims to be a learning reference, an experimentation
    base, and a starting point for services that expose Pokémon-related APIs.

    Overview

    - Purpose: provide an API and supporting components to manage and query Pokémon data while demonstrating best
      practices (TDD, hexagonal architecture, API contracts).
    - Audience: developers and contributors who want to run, test, and extend the project.

    Prerequisites

    - Java 17 or newer (OpenJDK/Temurin recommended)
    - Git
    - Docker (optional — required for some integration tests that use Testcontainers)

    Clone

    1. git clone <repo-url>
    2. cd my-pokedex

    Build & run

    Using the Gradle wrapper (recommended):

    ```bash
    ./gradlew clean build
    ./gradlew run
    ```

    Notes:

    - On Windows use `gradlew.bat` instead of `./gradlew`.
    - Build artifacts are produced under `build/libs`. The OpenAPI specification is generated under
      `build/classes/java/main/META-INF/swagger/`.

    Tests

    - Run unit and integration tests with:

    ```bash
    ./gradlew test
    ```

    - Note: some integration tests rely on Testcontainers and require Docker to be available on the host.

    Repository layout (high level)

    - `src/` — main source code
    - `specs/` — feature specifications, plans, contracts and tasks
    - `build.gradle.kts` — Gradle build configuration
    - `src/main/resources/application.yml` — runtime configuration
    - `build/` — generated artifacts and test reports

    Architecture & practices

    - Architecture: follows Hexagonal / Clean Architecture principles — separation between domain, ports/adapters and
      infrastructure.
    - Tests: Test-First (TDD) is expected for new features; each feature should include a specification in `specs/` and
      tests that initially fail.
    - Contracts: public APIs should include OpenAPI contracts in `specs/*/contracts/`.

    Contributing

    1. Create a branch using the pattern `###-feature-name` (for example `002-documentation-do-projeto`).
    2. Add or update a specification under `specs/` (templates are available in `.specify/templates`).
    3. PR contents should include:
       - Tests that initially fail demonstrating the desired behavior (unit/contract/integration).
       - Updated spec in `specs/` and `plan.md` when applicable.
       - Migration notes if the change breaks existing contracts.
    4. Open a Pull Request and wait for review. PRs must pass CI (linters, unit & contract tests).

    Troubleshooting

    - Docker unavailable for Testcontainers:
      - Install and start Docker Engine / Docker Desktop and make sure your user has permission to access the Docker
        socket.
    - Java version compatibility:
      - Install Java 17+ or use `./gradlew` which can provide a compatible runtime in certain environments.
    - Slow builds / dependency download issues:
      - Check the Gradle cache (`~/.gradle`) or configure a repository mirror.

    Useful links

    - Feature specifications: `specs/`
    - Current documentation feature: `specs/002-documentation-do-projeto/`

    Maintainers & contact

    - See the commit history and `AUTHORS` (if present). Open issues for bugs and feature requests.

    License

    - See `LICENSE` in the project root if present.

    Micronaut documentation (reference)

    - User Guide: https://docs.micronaut.io/4.9.3/guide/index.html
    - API Reference: https://docs.micronaut.io/4.9.3/api/index.html
    - Configuration Reference: https://docs.micronaut.io/4.9.3/guide/configurationreference.html
    - Micronaut Guides: https://guides.micronaut.io/index.html

    Other references

    - Micronaut Gradle Plugin: https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/
    - GraalVM Gradle Plugin: https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
    - Shadow Gradle Plugin: https://gradleup.com/shadow/
    - OpenAPI (Micronaut): https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html
    - OpenAPI: https://www.openapis.org
    - AOP (Micronaut): https://docs.micronaut.io/latest/guide/index.html#aop
    - Jetty Server (Micronaut): https://micronaut-projects.github.io/micronaut-servlet/latest/guide/index.html#jetty
    - Micronaut Test (REST-assured): https://micronaut-projects.github.io/micronaut-test/latest/guide/#restAssured
    - REST-assured: https://rest-assured.io/#docs
    - Hikari JDBC: https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc
    - Micronaut AOT: https://micronaut-projects.github.io/micronaut-aot/latest/guide/
    - Micronaut Test Resources: https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/
    - Mockito: https://site.mockito.org
    - JsonPath: https://github.com/json-path/JsonPath
    - Micronaut Guice: https://micronaut-projects.github.io/micronaut-guice/latest/guide/index.html
    - Lombok: https://projectlombok.org/features/all
    - Micronaut OpenAPI Explorer: https://micronaut-projects.github.io/micronaut-openapi/latest/guide/#openapiExplorer
    - OpenAPI Explorer (external): https://github.com/Authress-Engineering/openapi-explorer
    - Micronaut HTTP Client: https://docs.micronaut.io/latest/guide/index.html#nettyHttpClient
    - JUnit Platform Suite Engine: https://junit.org/junit5/docs/current/user-guide/#junit-platform-suite-engine-setup
    - Micronaut Validation: https://micronaut-projects.github.io/micronaut-validation/latest/guide/
    - MockServer Java client: https://www.mock-server.com/mock_server/mockserver_clients.html#java-mockserver-client
    - json-smart: https://netplex.github.io/json-smart/
    - Micronaut Data JDBC: https://micronaut-projects.github.io/micronaut-data/latest/guide/index.html#jdbc
    - Jakarta Annotations: https://jakarta.ee/specifications/annotations/

    OpenAPI (Swagger) documentation

    After a build the OpenAPI YAML is generated at `build/classes/java/main/META-INF/swagger/` (for example
    `my-pokedex-0.1.yml`).

    - UI (OpenAPI Explorer): http://localhost:8080/openapi-explorer/ (when the application is running)
    - Generated YAML: http://localhost:8080/swagger/my-pokedex-0.1.yml (or the file name produced by the build)

    To generate the artifacts run:

    ```bash
    ./gradlew clean build
    ```

    If the UI does not load, check that `classpath:META-INF/swagger` is present in `src/main/resources` or that the build
    wrote files to `build/classes/java/main/META-INF/swagger`.

Running with Docker Compose

This repository includes support for running the application locally with Docker Compose.

Core files

- `Dockerfile` — multi-stage build that uses the Gradle wrapper and produces a fat/shadow JAR
- `docker-compose.yml` — main compose file (db + app)
- `docker-compose.caffeine.override.yml` — override that sets `CACHE_STRATEGY=caffeine` (provided)
- `docker-compose.redis.override.yml` — override that sets `CACHE_STRATEGY=redis` and `REDIS_HOST`/`REDIS_PORT` (provided)
- `.dockerignore` — avoids sending heavy artifacts to the build context

Quick start (defaults)

```bash
docker compose up --build
```

The API will be available at http://localhost:8080 (Micronaut default).

Docker Compose will create a MySQL database named `mypokedex` with default credentials `pokedex`/`pokedex`.
Environment variables provided to the container include:

- `DATASOURCES_DEFAULT_URL` (default: `jdbc:mysql://db:3306/mypokedex`)
- `DATASOURCES_DEFAULT_USERNAME` (default: `pokedex`)
- `DATASOURCES_DEFAULT_PASSWORD` (default: `pokedex`)

Cache strategy support

This project supports two cache strategies:

- `caffeine` — in-memory (default)
- `redis` — external Redis instance (optional)

We provide two docker-compose override files to make it easy to switch:

- `docker-compose.caffeine.override.yml` — forces `CACHE_STRATEGY=caffeine`
- `docker-compose.redis.override.yml` — forces `CACHE_STRATEGY=redis` and sets `REDIS_HOST`/`REDIS_PORT`

Run with Caffeine (default) using the override explicitly:

```bash
docker compose -f docker-compose.yml -f docker-compose.caffeine.override.yml up --build -d
```

Run with Redis (start Redis service via the `redis` profile):

```bash
docker compose --profile redis -f docker-compose.yml -f docker-compose.redis.override.yml up --build -d
```

Short alternative (one-off environment variable):

```bash
CACHE_STRATEGY=redis docker compose --profile redis -f docker-compose.yml -f docker-compose.redis.override.yml up --build -d
```

Using a `.env` file (recommended for local development)

Create a `.env` file in the project root with values you want to reuse locally, for example:

```
CACHE_STRATEGY=redis
REDIS_HOST=redis
REDIS_PORT=6379
```

Then run:

```bash
docker compose --profile redis up --build -d
```

Verify environment inside the running `app` container:

```bash
docker compose exec app env | grep CACHE_STRATEGY
docker compose exec app env | grep REDIS_HOST
```

Useful checks

```bash
docker compose logs -f app
docker compose ps
docker compose --profile redis ps
```

Notes

- `REDIS_HOST` defaults to `redis` so the `app` will connect to the `redis` service when the `redis` profile is enabled.
- If you remove the host port mapping `6379:6379` from the `redis` service, Redis will still be available to the `app` inside
  the compose network but won't be exposed to the host machine.
- To run integration tests that rely on Docker/Testcontainers, make sure Docker is running and that your user has permission to
  access the Docker socket.

If you'd like, I can add a short `docs/development.md` with these commands and examples or a `.env.example` to the repo.


