#!/usr/bin/env bash
# Builds the JAR and installs it as a local bal tool named "library".
# Usage: ./install-local.sh

set -euo pipefail

TOOL_ID="library"
ORG="ballerinax"
NAME="tool_library"
VERSION="0.1.0-SNAPSHOT"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BALA_HOME="$HOME/.ballerina/repositories/local/bala"
TOOL_BALA="$BALA_HOME/$ORG/$NAME/$VERSION/any"
TOOL_LIBS="$TOOL_BALA/tool/libs"
BAL_TOOLS_TOML="$HOME/.ballerina/.config/bal-tools.toml"

echo "==> Cleaning up old installation..."
rm -rf "$BALA_HOME/$ORG/$NAME"

echo "==> Building JAR..."
cd "$SCRIPT_DIR"
./gradlew :native:jar

JAR="$SCRIPT_DIR/native/build/libs/bal-library-tool-$VERSION.jar"
if [ ! -f "$JAR" ]; then
    echo "ERROR: JAR not found at $JAR"
    exit 1
fi

echo "==> Installing tool JAR into $TOOL_LIBS ..."
mkdir -p "$TOOL_LIBS"
cp "$JAR" "$TOOL_LIBS/"

echo "==> Writing package.json..."
cp "$SCRIPT_DIR/Ballerina.toml" "$TOOL_BALA/"
BAL_VERSION=$(bal version | grep "^Ballerina" | awk '{print $2}')
cat > "$TOOL_BALA/package.json" <<JSON
{
  "organization": "$ORG",
  "name": "$NAME",
  "version": "$VERSION",
  "ballerina_version": "$BAL_VERSION",
  "platform": "java"
}
JSON

echo "==> Registering in bal-tools.toml..."
if [ -f "$BAL_TOOLS_TOML" ]; then
    python3 - <<PYEOF
import re
path = "$BAL_TOOLS_TOML"
with open(path) as f:
    content = f.read()
pattern = r'\[\[tool\]\][^\[]*id\s*=\s*"$TOOL_ID"[^\[]*'
content = re.sub(pattern, '', content)
content = content.strip() + "\n"
with open(path, 'w') as f:
    f.write(content)
PYEOF
fi

cat >> "$BAL_TOOLS_TOML" <<TOML

[[tool]]
id = "$TOOL_ID"
org = "$ORG"
name = "$NAME"
version = "$VERSION"
repository = "local"
active = true
TOML

echo ""
echo "Done! Try:"
echo "  bal library search http client"
echo "  bal library get ballerina/http"
