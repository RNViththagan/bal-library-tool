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

### 1. Add tool declaration to `Ballerina.toml`

```toml
[package]
org = "ballerinax"
name = "tool_library"
version = "0.1.0"
distribution = "2201.13.2"
authors = ["Ballerina"]
keywords = ["library", "search", "copilot"]
repository = "https://github.com/ballerina-platform/bal-library-tool"
license = ["Apache-2.0"]

[[tool]]
id = "library"
targetFile = "tool/libs/bal-library-tool-0.1.0.jar"
```

### 2. Build the fat JAR

```bash
export packageUser=<github-username>
export packagePAT=<github-pat>
./gradlew :native:jar
```

### 3. Copy JAR into the package directory

```bash
mkdir -p tool/libs
cp native/build/libs/bal-library-tool-0.1.0-SNAPSHOT.jar tool/libs/
```

Add `tool/libs/` to `.gitignore`.

### 4. Push to Ballerina Central

```bash
bal login
bal push
```

### 5. User installation (after publish)

```bash
bal tool pull library
bal library search http client
bal library get ballerina/http
```

---

## Verification

```bash
# 1. Build
./gradlew :native:jar

# 2. Copy JAR
mkdir -p tool/libs && cp native/build/libs/bal-library-tool-*.jar tool/libs/

# 3. Test local pack
bal pack

# 4. Push to Central
bal push

# 5. On a fresh machine
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
- The `[[tool]]` section in `Ballerina.toml` is the only addition needed before pushing
- The SPI entry at `META-INF/services/io.ballerina.cli.BLauncherCmd` wires `LibraryTool` as
  the `bal library` command handler
