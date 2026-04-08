# bal library tool

A Ballerina CLI tool for searching and retrieving library information from Ballerina Central.

## Commands

### Search libraries

```bash
bal library search <keywords...>
```

```bash
bal library search http client
bal library search kafka messaging
```

Returns a ranked list of matching libraries with descriptions, ordered by relevance.

### Get library details

```bash
bal library get <org/package> [<org/package>...]
```

```bash
bal library get ballerina/http
bal library get ballerina/http ballerinax/github
```

Returns full function signatures, parameter details, and type definitions.

## Installation

### Prerequisites

- [Ballerina](https://ballerina.io) 2201.13.2+
- Java 17+
- GitHub PAT with `read:packages` scope (for pulling the LS artifact)

### Install locally

```bash
export packageUser=<github-username>
export packagePAT=<github-pat>
./install-local.sh
```

### Uninstall

```bash
rm -rf ~/.ballerina/repositories/local/bala/ballerinax/tool_library/
```

Remove the `[[tool]]` entry for `library` from `~/.ballerina/.config/bal-tools.toml`.

## How it works

This tool is a thin CLI wrapper around the Ballerina Language Server's copilot library functionality.
It depends on the published `io.ballerina:ballerina-language-server` fat JAR from GitHub Packages,
which provides all the copilot classes, model-generator-commons, and bundled SQLite indexes.

- **Search** queries a bundled `search-index.sqlite` using FTS5 with weighted BM25 ranking
- **Get** resolves the package via the semantic model and extracts full API details

To update the LS version, change `lsVersion` in `gradle.properties`.

## License

Apache License 2.0
