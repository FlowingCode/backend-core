# Backend Core

Commons utilities for backend enterprise application development

## What is backend-core?

It's a set of common interfaces and implementations that allows to implement some of the principles of a clean architectural design for enterprise applications.

For more info, please refer to [Documentation](src/site/markdown/index.md)

Snapshots are available [here](https://maven.flowingcode.com/snapshots). 

## Download release

Release versions are available from Maven Central repository. Add the modules you need to your pom.xml file, for example:

```xml
<dependency>
   <groupId>com.flowingcode.backend-core</groupId>
   <artifactId>backend-core-business-spring-impl</artifactId>
   <version>X.Y.Z</version>
</dependency>
```

Available modules:

- `backend-core-model`
- `backend-core-data`
- `backend-core-data-impl`
- `backend-core-business`
- `backend-core-business-impl`
- `backend-core-business-spring-impl`

## Building

- git clone repository
- mvn clean install

## Release notes

See [here](https://github.com/FlowingCode/backend-core/releases)

## Issue tracking

Issues for this project are tracked [here](https://github.com/FlowingCode/backend-core/issues). All bug reports and feature requests are appreciated. 

## Contributions

Contributions are welcome, but there are no guarantees that they are accepted as such. Process for contributing is the following:

- Fork this project
- Create an issue to this project about the contribution (bug or feature) if there is no such issue about it already. Try to keep the scope minimal.
- Develop and test the fix or functionality carefully. Only include minimum amount of code needed to fix the issue.
- Refer to the fixed issue in commit
- Send a pull request for the original project
- Comment on the original issue that you have implemented a fix for it

## License & Author

Commons-Backend is distributed under Apache License 2.0. For license terms, see LICENSE.txt.

Commons-Backend is written by Flowing Code

# Developer Guide

Comming soon
