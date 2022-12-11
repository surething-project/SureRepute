<p align="center">
    <img src="../../sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">SureRepute-Client</i></h3>
<h4 align="center"><i>(Server REST API + PSQL DB)</i></h4>

---

## Table of Contents

- [Source Prerequisites](#source-prerequisites)
- [Resolve the Dependencies and Build the Server](#resolve-the-dependencies-and-build-the-server)
- [Run SureReputeClient](#run-surereputeclient)
- [Important Details](#important-details)
- [Authors](#authors)

## Source Prerequisites

- Java Development Kit (JDK) = 11
- Maven >= 3.8
- Build the [CA-Contract](../../Contract/CA-Contract)
- Build the [IdentityProvider-Contract](../../Contract/IdentityProvider-Contract)
- Build the [SureRepute-CS-Contract](../../Contract/SureRepute-CS-Contract)
- Having the CA.crt setup as trusted [CA](../CA)

# Resolve the Dependencies and Build the Server

From the root of the project execute:

```shell script
mvn clean install
```

## Run SureReputeClient

From the root of the project execute:

```shell script
mvn exec:java
```

## Important Details
- To use SureRepute-Client its SureRepute-Server, IdentityProvider, and CA must already be running
- To change urls of entities and identifications go to src/main/resources/client.properties:
  - ID=SureReputeClient1 (Client Identification)
  - SERVER_ID=SureReputeServer1 (SureRepute-Server Identification)
  - SERVER_URL=https://localhost:9092/v1 (SureRepute-Server url)
  - CA_URL=https://localhost:9090/v1 (CA url)
  - IDENTITY_PROVIDER_URL=https://localhost:9091/v1 (Identity Provider url)

## Authors

| Name              | University                 |                                                                                                                                                                                                                                                                                                                                                             More info |
|:------------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Rafael Figueiredo | Instituto Superior Técnico |     [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo") |