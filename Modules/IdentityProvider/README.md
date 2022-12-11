<p align="center">
    <img src="../../sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">Identity Provider</i></h3>
<h4 align="center"><i>(Server REST API + PSQL DB)</i></h4>

---

## Table of Contents

- [Build and Run from source locally](#build-and-run-form-source-locally)
  - [Source Prerequisites](#source-prerequisites)
  - [Add CA as a Trusted Certificate](#add-ca-as-a-trusted-certificate)
  - [Resolve the Dependencies and Build the Server](#resolve-the-dependencies-and-build-the-server)
  - [Run Identity Provider](#run-identity-provider)
  
- [Build the Identity Provider Docker image](#build-the-identity-provider-docker-image)
  - [Image Prerequisites](#image-prerequisites)
  - [Build Image](#build-image)

- [Run Locally with Docker Compose](#run-locally-with-docker-compose)
  - [Docker Compose Prerequisites](#docker-compose-prerequisites)
  - [Run Docker Compose](#run-docker-compose)

- [Deploy to Google Cloud](#deploy-to-google-cloud)
  - [Google Cloud Prerequisites](#google-cloud-prerequisites)
  - [Push the Identity Provider Docker Image to the GCloud Registry](#push-the-identity-provider-docker-image-to-the-gcloud-registry)
  - [Set up the cluster and the namespace](#set-up-the-cluster-and-the-namespace)
  - [Automatic Deployment](#automatic-deployment)

- [Authors](#authors)

## Build and Run form source locally

### Source Prerequisites

- Java Development Kit (JDK) = 11
- Maven >= 3.8
- Build the [CA-Contract](../../Contract/CA-Contract)
- Build the [IdentityProvider-Contract](../../Contract/IdentityProvider-Contract)
- Build the [SureRepute-SIP-Contract](../../Contract/SureRepute-SIP-Contract)
- Having the CA.crt created [CA](../CA)

## Add CA as a Trusted Certificate

Add CA Certificate to Java Trusted Certificates. _(You will need the password. (Default=changeit))_

```shell script
keytool -importcert -trustcacerts -cacerts -file CA.crt -alias SureReputeCA -storepass changeit
```

Note: If you had previously done this step, then you need to delete first the LedgerCA certificate.

```shell script
keytool -delete -alias SureReputeCA -trustcacerts -cacerts -storepass changeit
```

## Resolve the Dependencies and Build the Server

From the root of the module go to resources and initialize de database:

```shell script
cd src/main/resouces
./newDb.sh
```

From the root of the module execute:

```shell script
mvn clean install
```

Define the following environment variables:

```shell script
export IDENTITY_PROVIDER_URL=https://localhost:9091
export DB_CONNECTION=localhost
export DB_PORT=5432
export DB_NAME=identity_provider
export DB_USER=identity_provider
export DB_PWD=identity_provider
```

### Run Identity Provider

From the root of the module execute:

```shell script
mvn exec:java
```

## Build the Identity Provider Docker Image

### Image Prerequisites

- Docker >= 20.10.7
- Build the [CA-Contract](../../Contract/CA-Contract)
- Build the [IdentityProvider-Contract](../../Contract/IdentityProvider-Contract)
- Build the [SureRepute-SIP-Contract](../../Contract/SureRepute-SIP-Contract)

### Build Image

From the root of the module go to docker folder:
```shell script
cd docker
```

Build the image:
```shell script
./buildDockerImage.sh
```
Note: The urls of the servers need to be added to [url.properties](./src/main/resources/url.properties) before we set up the image. This can be done manually, or using [setupServerUrls](../setupServerUrls.sh), see modules [readme](../README.md) for more details.

## Run Locally with Docker Compose

### Docker Compose Prerequisites

- Docker >= 20.10.7
- [Build the Identity Provider Docker Image](#build-the-identity-provider-docker-image)
- docker-compose >= 1.29.2

### Run Docker Compose

From the root of the module, run docker-compose containing both the SureRepute Server and SureRepute PSQL services _(Use the -d
parameter to detach)_:

```shell script
docker-compose up
```

Whenever you want to stop the services execute:

```shell script
docker-compose down
```

Note: You can change the following environment variables in file [docker-compose.yml](docker/docker-compose.yml):

```shell script      
  - IDENTITY_PROVIDER_URL=https://localhost:9091
  - DB_CONNECTION=identity_provider-psql
  - DB_PORT=5433
  - DB_NAME=identity_provider
  - DB_USER=identity_provider
  - DB_PWD=identity_provider
```

## Deploy to Google Cloud

### Google Cloud Prerequisites

- Docker >= 20.10.7 (or other Container/VM Manager)
- kubectl >= 1.22.2
- [Build the Identity Provider Docker Image](#build-the-identity-provider-docker-image)

### Push the Identity Provider Docker Image to the GCloud Registry

Tag the image, with version 1.0.0:

```shell script
docker tag identity-provider-server gcr.io/gsdsupport/identity-provider-server:v1.0.0
```

Push to the registry:

```shell script
gcloud docker -- push gcr.io/gsdsupport/identity-provider-server:v1.0.0
```

### Set up the Cluster and the Namespace
From the root of the project, change directory to k8s/sure-repute-namespace

```shell script
cd k8s/identity-provider-namespace
```

If the cluster is not created then we run `deploy-namespace.sh` with create as argument to create the cluster and the namespace
```shell script
./deploy-namespace.sh create
```

If the cluster is already created we can just run `deploy-namespace.sh` which only creates the namespace
```shell script
./deploy-namespace.sh
```

To delete the cluster we run `deploy-namespace.sh` with delete as argument
```shell script
./deploy-namespace.sh delete
```

Note: This setup should be done by a kubernetes admin.

### Automatic Deployment

From the root of the project, change directory to k8s:

```shell script
cd k8s
```

Deploy the identity-provider chart:

```shell script
 helm install identity-provider ./identity-provider-chart
```
Note: You can change values present in [values.yaml](k8s/identity-provider-chart/values.yaml):
- DB_NAME: name of the database
- DB_USER: username for the database access (in base64)
- DB_PWD: password of the database (in base64)
- CIP_HOST: dns name for client communication

The command would be, for example:
```shell script
 helm install identity-provider ./identity-provider-chart --set DB_NAME=<DB_NAME>,CIP_HOST=<CIP_HOST>
```

To delete the identity provider chart we do:
```shell script
 helm uninstall surerepute
```

## Authors

| Name              | University                 |                                                                                                                                                                                                                                                                                                                                                             More info |
|:------------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Rafael Figueiredo | Instituto Superior Técnico |     [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo") |