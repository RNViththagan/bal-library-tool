# bal library tool

A Ballerina CLI tool for searching and retrieving library information from Ballerina Central.

## Commands

### Search libraries

Search for libraries by keywords:

```bash
bal library search <keywords...>
```

**Examples:**
```bash
bal library search http client
bal library search kafka messaging
bal library search fhir healthcare
```

Returns a ranked list of matching libraries with descriptions, ordered by relevance.

### Get library details

Retrieve full details of one or more libraries including functions, connectors, and types:

```bash
bal library get <org/package> [<org/package>...]
```

**Examples:**
```bash
bal library get ballerina/http
bal library get ballerina/http ballerinax/github
```

Returns full function signatures, parameter details, and type definitions for the specified libraries.

## Installation

### Prerequisites

- Ballerina installed ([ballerina.io](https://ballerina.io))
- Java 21+
- Gradle

### Install locally

```bash
./install-local.sh
```

This builds the fat JAR and registers it as a `bal` tool under `viththagan/library_tool`.

### Uninstall

Remove the entry from `~/.ballerina/.config/bal-tools.toml` and delete:
```
~/.ballerina/repositories/local/bala/viththagan/library-tool/
```

## How it works

- **Search** — queries a bundled `search-index.sqlite` using FTS5 with weighted BM25 ranking across package names, descriptions, keywords, types, connectors, and functions
- **Get** — resolves the package from Ballerina Central, compiles it, and extracts full API details (functions, connectors, types, parameters) via the semantic model; also uses a bundled `central-index.sqlite` as a pre-computed cache

Both SQLite indexes are bundled directly in the JAR and kept in sync with the Ballerina Language Server releases via an automated GitHub Actions workflow.

## Syncing indexes

The bundled SQLite indexes are automatically synced daily from the latest [ballerina-language-server](https://github.com/ballerina-platform/ballerina-language-server) release via `.github/workflows/sync-sqlite.yml`.

To manually sync to a specific release, trigger the workflow from the Actions tab with the desired tag (e.g. `v1.7.0`).

See [docs/syncing-from-ls.md](docs/syncing-from-ls.md) for details.

## License

Apache License 2.0 — see [LICENSE](LICENSE).
