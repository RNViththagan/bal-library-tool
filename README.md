# Ballerina Library Tool

A Ballerina CLI tool for searching and retrieving library information. It enables AI copilots and
developers to discover Ballerina packages and retrieve detailed API information including functions,
connectors, types, and parameters.

## Usage

### Search libraries

Search for libraries by keywords. Returns a ranked list ordered by relevance.

```bash
bal library search http client
bal library search kafka messaging
bal library search fhir healthcare
```

### Get library details

Retrieve full API details of one or more libraries including functions, connectors, and type definitions.

```bash
bal library get ballerina/http
bal library get ballerina/http ballerinax/github
```

## Install a Released Version

Grab the latest `bal-library-tool-<version>.zip` from the
[Releases page](https://github.com/RNViththagan/bal-library-tool/releases),
unzip it, and run the installer for your platform.

**macOS / Linux:**
```bash
unzip bal-library-tool-<version>.zip
cd bal-library-tool-<version>
./install.sh
```

**Windows (PowerShell):**
```powershell
Expand-Archive bal-library-tool-<version>.zip -DestinationPath .
Set-Location bal-library-tool-<version>
.\install.ps1
```

Both installers are fully offline and only need `bal` on your `PATH`.

## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 21 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

   >**Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.

2. Export GitHub credentials to pull build dependencies from GitHub Packages:
   ```bash
   export packageUser=<GitHub username>
   export packagePAT=<GitHub personal access token with read:packages scope>
   ```

### Building the Source

Execute the commands below to build from the source.

1. To build the tool:

        ./gradlew clean build

2. To build without running checks:

        ./gradlew clean build -x check

3. To run the tests:

        ./gradlew :native:test

4. To install locally as a `bal` tool:

        ./install-local.sh

5. To build against a specific language server version:

        ./gradlew clean build -PlsVersion=<x.y.z>

6. To build against a locally built language server JAR:

        ./gradlew clean build -PlsLocalJar=<path/to/ballerina-language-server.jar>

## Contributing to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

You can also check for [open issues](https://github.com/RNViththagan/bal-library-tool/issues) that
interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
