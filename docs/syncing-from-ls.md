# Syncing Resources from the Language Server

This tool bundles several resource files that originate from the
[Ballerina Language Server](https://github.com/ballerina-platform/ballerina-language-server) repository.
These files are **data only** — no code changes are required when syncing them.

## What needs to be synced

| File / Directory | LS source path | Sync when |
|------------------|----------------|-----------|
| `src/main/resources/search-index.sqlite` | extracted from `flow-model-generator/modules/flow-model-generator-ls-extension/build/libs/flow-model-generator-ls-extension-*.jar` | New packages or functions are added to Ballerina Central (LS release) |
| `src/main/resources/central-index.sqlite` | extracted from `flow-model-generator/modules/flow-model-generator-ls-extension/build/libs/flow-model-generator-ls-extension-*.jar` | Same as above |
| `src/main/resources/copilot/exclusion.json` | `flow-model-generator/modules/flow-model-generator-ls-extension/src/main/resources/copilot/exclusion.json` | Libraries or functions are added to or removed from the exclusion list |
| `src/main/resources/copilot/generic-services.json` | `flow-model-generator/modules/flow-model-generator-ls-extension/src/main/resources/copilot/generic-services.json` | New generic service definitions are added |
| `src/main/resources/copilot/instructions/**` | `flow-model-generator/modules/flow-model-generator-ls-extension/src/main/resources/copilot/instructions/` | Prompt instructions for a library are written or updated |
| `src/main/resources/inbuilt-triggers/**` | `misc/ls-extensions/modules/trigger-service/src/main/resources/inbuilt-triggers/` | Trigger listener definitions (parameters, types) are updated, or new trigger libraries are added |

## How to sync

Copy the changed files from your local LS checkout into this repo, then rebuild:

```bash
LS=<path-to-ballerina-language-server>

# SQLite indexes — extract from the built JAR
jar xf $LS/flow-model-generator/modules/flow-model-generator-ls-extension/build/libs/flow-model-generator-ls-extension-*.jar \
   search-index.sqlite central-index.sqlite
mv search-index.sqlite central-index.sqlite src/main/resources/

# exclusion + generic-services
cp $LS/flow-model-generator/modules/flow-model-generator-ls-extension/src/main/resources/copilot/exclusion.json \
   src/main/resources/copilot/
cp $LS/flow-model-generator/modules/flow-model-generator-ls-extension/src/main/resources/copilot/generic-services.json \
   src/main/resources/copilot/

# instructions (full directory sync)
rsync -av --delete \
   $LS/flow-model-generator/modules/flow-model-generator-ls-extension/src/main/resources/copilot/instructions/ \
   src/main/resources/copilot/instructions/

# inbuilt triggers (full directory sync)
rsync -av --delete \
   $LS/misc/ls-extensions/modules/trigger-service/src/main/resources/inbuilt-triggers/ \
   src/main/resources/inbuilt-triggers/
```

Then rebuild and reinstall the tool:

```bash
./install-local.sh
```

## Notes

- `search-index.sqlite` (~18 MB) and `central-index.sqlite` (~9 MB) are bundled directly in this repo
  under `src/main/resources/` and compiled into the fat JAR. They are updated manually before each
  tool release by extracting them from the latest `flow-model-generator-ls-extension-*.jar` build.
- Instruction markdown files (`copilot/instructions/**/*.md`) follow the path pattern
  `<org>/<package>/library.md`, `<org>/<package>/service.md`, or `<org>/<package>/test.md`.
  Adding a new file for a library is enough — no code changes needed.
- The `inbuilt-triggers/` files define listener parameters and service method signatures
  for event-driven libraries (Kafka, NATS, RabbitMQ, etc.).
  If a new trigger library is added in LS, copy its JSON file and it will be picked up automatically.
