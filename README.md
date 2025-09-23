# My Pokedex

Este repositório contém "My Pokedex", uma aplicação de exemplo construída sobre a plataforma JVM com Micronaut. O objetivo deste projeto é servir como referência para aprendizado, experiments e como base para serviços que expõem APIs relacionadas a Pokémons.

Resumo
- Propósito: fornecer uma API e conjunto de componentes para gerenciar e consultar dados de Pokémons, demonstrando boas práticas (TDD, arquitetura hexagonal, contratos).
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

- Arquitetura: o projeto segue princípios de Hexagonal / Clean Architecture — separação entre domínio, portas/adapters e infraestrutura.
- Testes: Test-First (TDD) obrigatório para novas features; cada feature inclui especificação em `specs/` e testes que devem falhar antes da implementação.
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
  - Solução: Instalar e configurar Docker Desktop ou engine; verificar que seu usuário tem permissão para acessar o socket Docker.
- Erro: Java incompatível
  - Solução: Instalar Java 17+ ou usar `./gradlew` que baixa runtime adequado em alguns setups.
- Build lento / problemas de rede ao baixar dependências
  - Solução: Verificar cache do Gradle (`~/.gradle`) ou configurar mirror de repositório.

Links úteis
- Especificações de features: `specs/`
- Planos e tarefas da feature de documentação atual: `specs/002-documentação-do-projeto/`

Contato e manutenção

- Mantenedores: ver histórico de commits e `AUTHORS` (se existir) — abra issues para bugs e solicitações de features.

Licença

- Licença do projeto: consulte `LICENSE` na raiz do repositório (se presente).
## Micronaut 4.9.3 Documentation

- [User Guide](https://docs.micronaut.io/4.9.3/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.9.3/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.9.3/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Micronaut Gradle Plugin documentation](https://micronaut-projects.github.io/micronaut-gradle-plugin/latest/)
- [GraalVM Gradle Plugin documentation](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [Shadow Gradle Plugin](https://gradleup.com/shadow/)
## Feature openapi documentation

- [Micronaut OpenAPI Support documentation](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)

- [https://www.openapis.org](https://www.openapis.org)


## Feature micronaut-aop documentation

- [Micronaut Aspect-Oriented Programming (AOP) documentation](https://docs.micronaut.io/latest/guide/index.html#aop)


## Feature jetty-server documentation

- [Micronaut Jetty Server documentation](https://micronaut-projects.github.io/micronaut-servlet/latest/guide/index.html#jetty)


## Feature micronaut-test-rest-assured documentation

- [Micronaut Micronaut-Test REST-assured documentation](https://micronaut-projects.github.io/micronaut-test/latest/guide/#restAssured)

- [https://rest-assured.io/#docs](https://rest-assured.io/#docs)


## Feature jdbc-hikari documentation

- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)


## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)


## Feature test-resources documentation

- [Micronaut Test Resources documentation](https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/)


## Feature mockito documentation

- [https://site.mockito.org](https://site.mockito.org)


## Feature json-path documentation

- [https://github.com/json-path/JsonPath](https://github.com/json-path/JsonPath)


## Feature guice documentation

- [Micronaut Guice documentation](https://micronaut-projects.github.io/micronaut-guice/latest/guide/index.html)


## Feature lombok documentation

- [Micronaut Project Lombok documentation](https://docs.micronaut.io/latest/guide/index.html#lombok)

- [https://projectlombok.org/features/all](https://projectlombok.org/features/all)


## Feature openapi-explorer documentation

- [Micronaut OpenAPI Explorer View documentation](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/#openapiExplorer)

- [https://github.com/Authress-Engineering/openapi-explorer](https://github.com/Authress-Engineering/openapi-explorer)


## Feature http-client documentation

- [Micronaut HTTP Client documentation](https://docs.micronaut.io/latest/guide/index.html#nettyHttpClient)


## Feature junit-platform-suite-engine documentation

- [https://junit.org/junit5/docs/current/user-guide/#junit-platform-suite-engine-setup](https://junit.org/junit5/docs/current/user-guide/#junit-platform-suite-engine-setup)


## Feature validation documentation

- [Micronaut Validation documentation](https://micronaut-projects.github.io/micronaut-validation/latest/guide/)


## Feature mockserver-client-java documentation

- [https://www.mock-server.com/mock_server/mockserver_clients.html#java-mockserver-client](https://www.mock-server.com/mock_server/mockserver_clients.html#java-mockserver-client)


## Feature json-smart documentation

- [https://netplex.github.io/json-smart/](https://netplex.github.io/json-smart/)


## Feature data-jdbc documentation

- [Micronaut Data JDBC documentation](https://micronaut-projects.github.io/micronaut-data/latest/guide/index.html#jdbc)


## Feature annotation-api documentation

- [https://jakarta.ee/specifications/annotations/](https://jakarta.ee/specifications/annotations/)

## Rodando com Docker Compose

Foram adicionados os arquivos necessários para executar a aplicação localmente com Docker Compose:

- `Dockerfile` - build multi-stage que usa o Gradle wrapper e produz um fat/shadow jar
- `docker-compose.yml` - define os serviços `db` (MySQL) e `app`
- `.dockerignore` - evita enviar artefatos pesados para o contexto de build

Instruções rápidas:

```bash
docker compose up --build
```

A API ficará disponível em http://localhost:8080 (porta padrão do Micronaut).

O Compose cria um banco MySQL com database `mypokedex` e usuário `pokedex`/`pokedex`.
As variáveis passadas ao container são:

- DATASOURCES_DEFAULT_URL (padrão: jdbc:mysql://db:3306/mypokedex)
- DATASOURCES_DEFAULT_USERNAME (padrão: pokedex)
- DATASOURCES_DEFAULT_PASSWORD (padrão: pokedex)

Se preferir usar outro banco, sobrescreva as variáveis no `docker-compose.yml` ou defina no ambiente antes de executar.

Observações:

- O Dockerfile executa `./gradlew shadowJar`; na primeira build isso pode demorar. Rebuilds subsequentes aproveitarão cache de camadas.
- Se já tiver o jar pronto em `build/libs/*all*.jar`, é possível simplificar o fluxo de build (por exemplo, usar imagem base e apenas copiar o jar).


