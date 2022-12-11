<p align="center">
    <img src="../../sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">SureRepute Server</i></h3>
<h4 align="center"><i>(Server REST API + PSQL DB)</i></h4>

---

## Table of Contents
- [Build and Run from source locally](#build-and-run-form-source-locally)
  - [Source Prerequisites](#source-prerequisites)
  - [Add CA as a Trusted Certificate](#add-ca-as-a-trusted-certificate)
  - [Resolve the Dependencies and Build the Server](#resolve-the-dependencies-and-build-the-server)
  - [Run SureRepute-Server](#run-surerepute-server)

- [Build the SureRepute-Server Docker image](#build-the-surerepute-server-docker-image)
  - [Image Prerequisites](#image-prerequisites)
  - [Build Image](#build-image)
  
- [Run Locally with Docker Compose](#run-locally-with-docker-compose)
  - [Docker Compose Prerequisites](#docker-compose-prerequisites)
  - [Run Docker Compose](#run-docker-compose)

- [Deploy to Google Cloud](#deploy-to-google-cloud)
  - [Google Cloud Prerequisites](#google-cloud-prerequisites)
  - [Push the SureRepute-Server Docker Image to the GCloud Registry](#push-the-surerepute-server-docker-image-to-the-gcloud-registry)
  - [Set up the cluster and the namespace](#set-up-the-cluster-and-the-namespace)
  - [Automatic Deployment](#automatic-deployment)

- [Authors](#authors)

## Build and Run form source locally

### Source Prerequisites

- Java Development Kit (JDK) = 11
- Maven >= 3.8
- Build the [SureRepute-CS-Contract](../../Contract/SureRepute-CS-Contract)
- Build the [SureRepute-SS-Contract](../../Contract/SureRepute-SS-Contract)
- Build the [SureRepute-SIP-Contract](../../Contract/SureRepute-SIP-Contract)
- Build the [CA-Contract](../../Contract/CA-Contract)

- Have the CA.crt created [CA](../CA)

### Add CA as a Trusted Certificate

Add CA Certificate to Java Trusted Certificates. _(You will need the password. (Default=changeit))_

```shell script
keytool -importcert -trustcacerts -cacerts -file SureReputeCA.crt -alias SureReputeCA -storepass changeit
```

Note: If you had previously done this step, then you need to delete first the LedgerCA certificate.

```shell script
keytool -delete -alias SureReputeCA -trustcacerts -cacerts -storepass changeit
```

### Resolve the Dependencies and Build the Server

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
export ID=SureReputeServer1
export CLIENT_SERVER_URL=https://localhost:9092
export SERVER_SERVER_URL=https://localhost:9093
export IP_SERVER_URL=https://localhost:9094
export DB_CONNECTION=localhost
export DB_PORT=5432
export DB_NAME=sure_repute1
export DB_USER=sure_repute1
export DB_PWD=sure_repute1
```

### Run SureRepute-Server

From the root of the module execute:

```shell script
mvn exec:java
```

## Build the SureRepute-Server Docker Image

### Image Prerequisites

- Docker >= 20.10.7
- Build the [SureRepute-CS-Contract](../../Contract/SureRepute-CS-Contract)
- Build the [SureRepute-SS-Contract](../../Contract/SureRepute-SS-Contract)
- Build the [SureRepute-SIP-Contract](../../Contract/SureRepute-SIP-Contract))
- Build the [CA-Contract](../../Contract/CA-Contract)


### Build Image
From the root of the module go to docker folder:
```shell script
cd docker
```

Build the image:
```shell script
./buildDockerImage.sh
```
Note: When running more than one server, the urls of the servers need to be added to [url.properties](./src/main/resources/url.properties) before we set up the image. This can be done manually, or using [setupServerUrls](../setupServerUrls.sh), see modules [readme](../README.md) for more details.

## Run Locally with Docker Compose

### Docker Compose Prerequisites

- Docker >= 20.10.7
- [Build the SureRepute-Server Docker Image](#build-the-surerepute-server-docker-image)
- docker-compose >= 1.29.2

### Run Docker Compose

From the root of the module go to docker folder:
```shell script
cd docker
```

Run docker-compose containing both the SureRepute Server and SureRepute PSQL services _(Use the -d
parameter to detach)_:

```shell script
docker-compose up
```

Note: You can change the following environment variables in file [docker-compose.yml](docker/docker-compose.yml):
```shell script
  - ID=SureReputeServer1
  - CLIENT_SERVER_URL=https://localhost:9092
  - SERVER_SERVER_URL=>https://localhost:9093
  - IP_SERVER_URL=https://localhost:9094
  - DB_CONNECTION=sure_repute-psql
  - DB_PORT=5432
  - DB_NAME=sure_repute1
  - DB_USER=sure_repute1
  - DB_PWD=sure_repute1
```

Whenever you want to stop the services execute:

```shell script
docker-compose down
```

## Deploy to Google Cloud

### Google Cloud Prerequisites

- Docker >= 20.10.7 (or other Container/VM Manager)
- kubectl >= 1.22.2
- [Build the SureRepute Server Docker Image](#build-the-surerepute-server-docker-image)

### Push the SureRepute Server Docker Image to the GCloud Registry

Tag the image, with version 1.0.0:

```shell script
docker tag sure-repute-server gcr.io/gsdsupport/sure-repute-server:v1.0.0
```

Push to the registry:

```shell script
gcloud docker -- push gcr.io/gsdsupport/sure-repute-server:v1.0.0
```

Note: This setup should be done by a kubernetes admin. 

### Set up the Cluster and the Namespace
From the root of the project, change directory to k8s/sure-repute-namespace

```shell script
cd k8s/sure-repute-namespace
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

Deploy the surerepute chart:

```shell script
 helm install surerepute ./sure-repute-chart
```

This command installs surerepute with the default values present in [values.yaml](k8s/sure-repute-chart/values.yaml).
If you want to run more than one server you can do this command multiple times, and you just need to change the values you want.

The mandatory values you need to change are:
- K8S_ID: The id used on the deployment of the k8s, which is unique
- ID: The id used to identify this server, which is unique
- CS_HOST_NAME: The dns name for client communication with this server
- SS_HOST_NAME: The dns name for server communication with this server
- SIP_HOST_NAME: The dns name for identity-provider communication with this server

The command would be:
```shell script
 helm install surerepute-<K8S_ID> ./sure-repute-chart --set K8S_ID=<K8S_ID>,ID=<ID>,SS_HOST_NAME:<SS_HOST_NAME>,CS_HOST_NAME=<CS_HOST_NAME>,SIP_HOST_NAME=<SIP_HOST_NAME>
```
Note: You can change other values like the username and password of the database.

To delete the surerepute chart we do:
```shell script
 helm uninstall surerepute
```

## Authors

| Name              | University                 |                                                                                                                                                                                                                                                                                                                                                             More info |
|:------------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Rafael Figueiredo | Instituto Superior Técnico |     [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo") |