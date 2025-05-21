[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE.txt)

# Backend Core

Common utilities for backend enterprise application development.

Backend Core provides a set of modular libraries to help you implement clean architecture (three-tier, hexagonal, DDD) in your enterprise Java applications. It includes:

- **Model layer**: domain interfaces and DTOs (`backend-core-model`)
- **Data layer**: persistence contracts (`backend-core-data`) and JPA-based implementations (`backend-core-data-impl`)
- **Service layer**: business logic contracts (`backend-core-business`) and default implementations (`backend-core-business-impl`)
- **Spring Boot integration**: auto-configuration support (`backend-core-business-spring-impl`)

## Features

- Generic CRUD interfaces and base implementations
- Strongly-typed filtering and query support
- Reusable service layering and transactional support
- Spring Boot integration for easy wiring
- Fully documented design and examples

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.x

### Build from Source

```bash
git clone https://github.com/FlowingCode/backend-core.git
cd backend-core
mvn clean install
```

### Generate Documentation

```bash
mvn site
# Open target/site/index.html in your browser
```

## Usage

### Available Modules

| Module                             | Description                                                          |
|------------------------------------|----------------------------------------------------------------------|
| `backend-core-model`               | Domain interfaces, DTOs, exceptions and validation                   |
| `backend-core-data`                | DAO contracts (CRUD, Query, etc.)                                    |
| `backend-core-data-impl`           | JPA implementations for DAO contracts                                |
| `backend-core-business`            | Service contracts (business logic interfaces)                        |
| `backend-core-business-impl`       | Default implementations for business/service contracts               |
| `backend-core-business-spring-impl`| Spring Boot auto-configuration for services and repositories          |

Add the desired module(s) to your project's dependencies:

```xml
<!-- Replace <artifactId> with the module(s) you need -->
<dependency>
  <groupId>com.flowingcode.backend-core</groupId>
  <artifactId>backend-core-data</artifactId>
  <version>1.1.0-SNAPSHOT</version>
</dependency>
```

Snapshots are available at:

```xml
<repository>
  <id>flowingcode-snapshots</id>
  <url>https://maven.flowingcode.com/snapshots</url>
  <snapshots>
    <enabled>true</enabled>
  </snapshots>
</repository>
```

For release versions, see Maven Central (coming soon).

## Documentation

Detailed design documentation and API reference are available via the Maven Site and in the source Markdown docs:

- [Maven Site](target/site/index.html)
- [Design documentation](src/site/markdown/index.md)

## Release Notes

See the [GitHub releases](https://github.com/FlowingCode/backend-core/releases).

## Issue Tracking

Report bugs and request features on [GitHub Issues](https://github.com/FlowingCode/backend-core/issues).

## Contributing

Contributions are welcome! Please follow the process outlined below:

1. Fork this repository.
2. Create an issue for your contribution (bug or feature request), or select an existing one.
3. Develop and test your changes carefully; include only the minimum code required.
4. Reference the issue in your commit messages.
5. Send a pull request and comment on the issue once it's ready.

## License

Apache License 2.0. See [LICENSE.txt](LICENSE.txt).
