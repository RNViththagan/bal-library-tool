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

## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 21 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

   >**Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.

2. Export GitHub Personal access token with read package permissions as follows,
   ```bash
   export packageUser=<Username>
   export packagePAT=<Personal access token>
   ```

### Building the Source

Execute the commands below to build from the source.

1. To build the library:

        ./gradlew clean build

2. To build the module without the checks:

        ./gradlew clean build -x check

3. To install locally as a bal tool:

        ./install-local.sh

4. To publish to maven local:

        ./gradlew clean build publishToMavenLocal

## Contributing to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

You can also check for [open issues](https://github.com/ballerina-platform/bal-library-tool/issues) that
interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

* Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
* Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
