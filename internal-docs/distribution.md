# Distribution Plan

## Goal

Publish the tool to Ballerina Central so any user can install it with:

```bash
bal tool pull library
```

## Status

Pending — needs discussion with the Ballerina team regarding:
- Publishing under `ballerinax` org on Ballerina Central
- Whether the tool should be officially supported or community-maintained
- Versioning and release cadence aligned with LS releases

---

## How Ballerina tool distribution works

Ballerina tools are published as `.bala` packages to Ballerina Central. The tool JAR must be
bundled inside the `.bala` under `tool/libs/`.

```
<org>/<name>/<version>/<platform>/
├── Ballerina.toml
├── package.json
└── tool/libs/
    └── bal-library-tool-<version>.jar
```

Once published, users install with:
```bash
bal tool pull library
bal tool pull library:0.1.0    # specific version
```

And uninstall with:
```bash
bal tool remove library
```

---

## What needs to be done

The `ballerina/` subproject uses `io.ballerina.plugin` for bala packaging. All Ballerina.toml and
BalTool.toml files are generated from templates at build time — no manual edits needed.

### 1. Build and pack the bala

```bash
export packageUser=<github-username>
export packagePAT=<github-pat>
./gradlew clean build
```

This runs `updateTomlFiles` (generates `ballerina/Ballerina.toml` and `ballerina/BalTool.toml`)
and `bal pack` inside the `ballerina/` subproject, producing the `.bala` artifact.

### 2. Push to Ballerina Central

```bash
bal login
cd ballerina && bal push
```

### 3. User installation (after publish)

```bash
bal tool pull library
bal library search http client
bal library get ballerina/http
```

---

## Local installation options

### Option A — `install-local.sh` (quick, no jballerina-tools download)

Builds the native JAR and installs directly into `~/.ballerina/repositories/local/bala`.
Fastest option for iterating on local changes.

```bash
./install-local.sh
```

### Option B — Gradle full build with local Central

Uses `io.ballerina.plugin` to produce the bala and install it via the local Central registry.
Requires downloading jballerina-tools (~400 MB, cached after first run).

```bash
./gradlew clean build -PpublishToLocalCentral=true
```

---

## Verification

```bash
export packageUser=<github-username>
export packagePAT=<github-pat>

# 1. Run tests
./gradlew :native:test

# 2. Build
./gradlew clean build

# 3. Install locally
./install-local.sh

# 4. Smoke test
bal library search http client
bal library get ballerina/http

# 5. Push to Central
cd ballerina && bal push

# 6. On a fresh machine
bal tool pull library
bal library search http client
bal library get ballerina/http
```

---

## Notes

- The fat JAR is ~21 MB — bundles only the LS copilot classes, model-generator-commons,
  sqlite-jdbc, and the bundled SQLite indexes/resources
- The `org.ballerinalang` dependencies (ballerina-lang, ballerina-cli, etc.) are NOT bundled —
  they're already on the Ballerina runtime classpath
- When the LS ships a new release, bump `lsVersion` in `gradle.properties` and rebuild
- `ballerina/Ballerina.toml` and `ballerina/BalTool.toml` are generated — do not edit them directly;
  edit the templates in `build-config/resources/package/` instead
- The SPI entry at `META-INF/services/io.ballerina.cli.BLauncherCmd` wires `LibraryTool` as
  the `bal library` command handler
