<p align="center">
  <img src="./../../sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">IdentityProvider-Contract</i></h3>
<h4 align="center"><i>By Rafael Figueiredo</i></h4>
<h4 align="center"><i>(Protobuf + OpenAPI)</i></h4>

---

<p align = "center">Protobuf data type definitions and API service specification of Identity Provider</p>

## Table of Contents

- [Structure](#structure)
- [Build the Contract](#build-the-contract)
    - [Contract Prerequisites](#contract-prerequisites)
    - [Resolve the Dependencies and Build the Contract](#resolve-the-dependencies-and-build-the-contract)
- [Rest API Specification](#rest-api-specification)
  - [Prerequisites](#prerequisites)
  - [OpenAPI Documentation](#openapi-documentation)
- [Authors](#authors)

## Structure

| Directory              |            Description             |
|:-----------------------|:----------------------------------:|
| [src](src)             | Protobuf data message definitions  |
| [doc](doc)             | REST API specification _(OpenAPI)_ |

## Build the Contract

### Contract Prerequisites

- Java Development Kit (JDK) >= 11
- Maven >= 3.8

### Resolve the Dependencies and Build the Contract

From the root execute:

```shell script
mvn clean install
```

## REST API Specification

### Prerequisites

- Go >= 1.17
- Python >= 3.8.10
- Protocol Buffer Compiler (Protoc) >= 3.6.1
- [protoc-gen-openapi](https://github.com/google/gnostic/tree/master/cmd/protoc-gen-openapi) plugin

### OpenAPI Documentation

- Contains an [openapi.html](doc/openapi.html) that specifies the REST API and Data Specification



## Authors

| Name              | University                 | More info                                                                                                                                                                                                                                                                                                                                                         |
|-------------------|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rafael Figueiredo | Instituto Superior Técnico | [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo") |
