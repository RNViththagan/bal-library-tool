# bal-library-tool — System Design & Architecture

## Context

This tool is a thin CLI wrapper around the Ballerina Language Server's copilot library functionality.
Instead of maintaining copies of LS classes, it depends on the published
`io.ballerina:ballerina-language-server` fat JAR from GitHub Packages and selectively bundles only
the classes and resources it needs.

---

## Commands

```bash
bal library search <keywords...>   # keyword search → ranked libraries (name + description)
bal library get <libnames...>      # full details: clients, functions, typeDefs
```

---

## Architecture

```
┌────────────────────────────────────────────────────────────────┐
│                        bal library                             │
│                       (LibraryTool)                            │
│                    io.ballerina.library.cli                    │
└────────────────┬───────────────────────────┬───────────────────┘
                 │                           │
       "search"  │                           │  "get"
    ┌────────────▼────────────┐   ┌──────────▼─────────────┐
    │   LibrarySearchService  │   │   LibraryGetService     │
    │  io.ballerina.library   │   │  io.ballerina.library   │
    │       .service          │   │       .service          │
    └────────────┬────────────┘   └──────────┬─────────────┘
                 │                           │
                 └──────────┬────────────────┘
                            │
              ┌─────────────▼──────────────┐
              │    CopilotLibraryManager    │  ← from LS fat JAR
              │ io.ballerina.flowmodel     │
              │  generator.core.copilot    │
              └─────────────┬──────────────┘
                            │
            ┌───────────────┼───────────────┐
            │               │               │
   ┌────────▼──────┐ ┌─────▼──────┐ ┌──────▼───────┐
   │ LibraryDB     │ │ Symbol     │ │ Instruction  │
   │ Accessor      │ │ Processor  │ │ Loader       │
   │ (FTS5 search) │ │ (semantic  │ │ (markdown    │
   │               │ │  model)    │ │  resources)  │
   └───────┬───────┘ └─────┬──────┘ └──────────────┘
           │               │
   ┌───────▼───────┐ ┌─────▼──────┐
   │ search-index  │ │ central-   │
   │ .sqlite       │ │ index      │
   │               │ │ .sqlite    │
   └───────────────┘ └────────────┘
```

All boxes below the service layer come from the LS fat JAR — we don't maintain any of that code.

---

## Our code (3 files)

| Class | Package | Responsibility |
|-------|---------|----------------|
| `LibraryTool` | `io.ballerina.library.cli` | `BLauncherCmd` impl, picocli command, dispatches to services |
| `LibrarySearchService` | `io.ballerina.library.service` | Calls `CopilotLibraryManager.getLibrariesBySearch()`, returns JSON |
| `LibraryGetService` | `io.ballerina.library.service` | Calls `CopilotLibraryManager.loadFilteredLibraries()`, returns JSON via `ModelToJsonConverter` |

---

## LS classes we use (from the fat JAR)

| Class | LS Package | Role |
|-------|-----------|------|
| `CopilotLibraryManager` | `io.ballerina.flowmodelgenerator.core.copilot` | Core orchestrator for search, get, and exclusions |
| `LibraryDatabaseAccessor` | `...copilot.database` | FTS5 queries against search-index.sqlite |
| `SymbolProcessor` | `...copilot.util` | Walks semantic model, extracts clients/functions/typeDefs |
| `LibraryModelConverter` | `...copilot.util` | Converts compiler symbols → model objects |
| `TypeSymbolExtractor` | `...copilot.util` | Extracts type information from symbols |
| `TypeDefDataBuilder` | `...copilot.builder` | Builds TypeDefData for record/enum/union/class types |
| `TypeLinkBuilder` | `...copilot.builder` | Builds type links for cross-references |
| `ServiceLoader` | `...copilot.service` | Loads trigger/service definitions |
| `InstructionLoader` | `io.ballerina.flowmodelgenerator.core` | Loads copilot instruction markdown files |
| `ModelToJsonConverter` | `...copilot.model` | Serializes Library objects to JSON |
| `SearchDatabaseManager` | `io.ballerina.modelgenerator.commons` | Singleton; extracts SQLite from JAR, provides JDBC |
| `FunctionDataBuilder` | `io.ballerina.modelgenerator.commons` | Builds FunctionData from compiler symbols |
| `PackageUtil` | `io.ballerina.modelgenerator.commons` | Resolves packages, gets semantic models |
| `CommonUtils` | `io.ballerina.modelgenerator.commons` | Type resolution utilities |

---

## Bundled resources (from the LS fat JAR)

| Resource | Purpose |
|----------|---------|
| `search-index.sqlite` | FTS5 search index — powers `bal library search` |
| `central-index.sqlite` | Pre-computed FunctionData cache — speeds up `bal library get` |
| `copilot/exclusion.json` | Libraries/functions excluded from results |
| `copilot/generic-services.json` | Generic service templates |
| `copilot/instructions/**` | Library-specific markdown instructions |
| `inbuilt-triggers/*.json` | Listener/service definitions for event-driven libraries |

All resources are bundled inside the LS fat JAR and selectively extracted into our tool JAR
at build time via the `lsRuntime` Gradle configuration with include filters.

---

## Project structure

```
bal-library-tool/
├── build.gradle              ← root: plugins + allprojects repos
├── settings.gradle           ← pluginManagement, includes ':native' and ':ballerina'
├── gradle.properties         ← ALL versions (lsVersion, ballerinaLangVersion, etc.)
├── install-local.sh          ← quick local dev install (no jballerina-tools download)
├── README.md
├── build-config/
│   └── resources/package/
│       ├── Ballerina.toml    ← template (replaces @toml.version@ at build time)
│       └── BalTool.toml      ← template with tool id + native JAR path
├── ballerina/
│   ├── build.gradle          ← io.ballerina.plugin subproject for bala packaging
│   ├── Ballerina.toml        ← generated from template at build time
│   └── BalTool.toml          ← generated from template at build time
└── native/
    ├── build.gradle          ← Java subproject: deps + jar task + test config
    └── src/
        ├── main/
        │   ├── java/io/ballerina/library/
        │   │   ├── cli/LibraryTool.java
        │   │   └── service/
        │   │       ├── LibraryGetService.java
        │   │       └── LibrarySearchService.java
        │   └── resources/META-INF/services/
        │       └── io.ballerina.cli.BLauncherCmd
        └── test/
            ├── java/io/ballerina/library/
            │   ├── TestUtil.java
            │   ├── LibraryToolTest.java
            │   ├── LibrarySearchServiceTest.java
            │   └── LibraryGetServiceTest.java
            └── resources/command-outputs/unix/
                ├── help.txt
                ├── search-no-args.txt
                ├── get-no-args.txt
                └── unknown-subcommand.txt
```

---

## Dependencies

### From Maven / GitHub Packages (implementation — on compile classpath)

| Dependency | Source | Purpose |
|------------|--------|---------|
| `org.ballerinalang:ballerina-lang` | Maven Central / GH Packages | Compiler API, semantic model |
| `org.ballerinalang:ballerina-tools-api` | same | Tools API |
| `org.ballerinalang:ballerina-cli` | same | `BLauncherCmd` interface |
| `org.ballerinalang:ballerina-parser` | same | Parser API |
| `org.ballerinalang:ballerina-runtime` | same | `IdentifierUtils` |
| `info.picocli:picocli` | Maven Central | CLI argument parsing |
| `com.google.code.gson:gson` | Maven Central | JSON serialization |
| `io.ballerina:ballerina-language-server` | GH Packages | LS fat JAR (copilot classes + resources) |

### Bundled in tool JAR (extracted from LS fat JAR via `lsRuntime` config)

| What | Why |
|------|-----|
| `io.ballerina.flowmodelgenerator.core.copilot.**` | Copilot library manager, model, utils |
| `io.ballerina.flowmodelgenerator.core.InstructionLoader` | Instruction loading |
| `io.ballerina.modelgenerator.commons.**` | FunctionData, PackageUtil, SearchDatabaseManager |
| `org.sqlite.**` + native libs | SQLite JDBC driver (not on Ballerina classpath) |
| `*.sqlite`, `copilot/**`, `inbuilt-triggers/**` | Bundled resources |

All `org.ballerinalang` deps are already on the Ballerina runtime classpath — they are NOT bundled
in the tool JAR.

---

## Version management

All versions live in `gradle.properties`:

```properties
lsVersion=1.7.0.alpha5              # ← bump this to update LS classes + resources
ballerinaLangVersion=2201.13.2      # ← matches Ballerina distribution version
```

To update to a new LS release: change `lsVersion`, rebuild, reinstall. No code changes needed.

---

## Tests

Tests live in `native/src/test/`. They use TestNG and cover:

| Class | What it tests |
|-------|---------------|
| `LibraryToolTest` | CLI output: `--help`, unknown subcommand, missing args |
| `LibrarySearchServiceTest` | `search("http")` returns results including `ballerina/http` |
| `LibraryGetServiceTest` | `get("ballerina/http")` returns typeDefs and full details |

`LibraryGetServiceTest` requires a Ballerina distribution for package resolution. The build
automatically uses `target/ballerina-runtime` (populated by `./gradlew :ballerina:build`) or
falls back to the locally installed distribution at `/Library/Ballerina/distributions/ballerina-<version>`.

```bash
./gradlew :native:test
```

---

## Build & install

```bash
export packageUser=<github-username>
export packagePAT=<github-pat-with-read:packages>

# Quick local dev (no jballerina-tools download, ~seconds)
./install-local.sh

# Full bala packaging via io.ballerina.plugin (downloads jballerina-tools ~400MB on first run)
./gradlew clean build -PpublishToLocalCentral=true
```
