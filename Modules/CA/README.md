<p align="center">
    <img src="../../sureThing.png" width="70" height="100" alt="CROSS Logo"/>
</p>

<h3 align="center">Certificate Authority</i></h3>
<h4 align="center"><i>(Server REST API)</i></h4>

---

## Table of Contents

- [Build and Run from source locally](#build-and-run-from-source-locally)
  - [Source Prerequisites](#source-prerequisites)
  - [Setup CA.crt as trusted](#setup-cacrt-as-trusted)
  - [Run CA](#run-ca)

- [Build the CA Docker image](#build-the-ca-docker-image)
  - [Image Prerequisites](#image-prerequisites)
  - [Build Image](#build-image)

- [Run Locally with Docker Compose](#run-locally-with-docker-compose)
  - [Docker Compose Prerequisites](#docker-compose-prerequisites)
  - [Run Docker Compose](#run-docker-compose)

- [Deploy to Google Cloud](#deploy-to-google-cloud)
  - [Google Cloud Prerequisites](#google-cloud-prerequisites)
  - [Push the CA Docker Image to the GCloud Registry](#push-the-ca-docker-image-to-the-gcloud-registry)
  - [Set up the cluster and the namespace](#set-up-the-cluster-and-the-namespace)
  - [Automatic Deployment](#automatic-deployment)

- [Authors](#authors)

## Build and Run from source locally

### Source Prerequisites

- Java Development Kit (JDK) = 11
- Maven >= 3.8
- Build the [CA-Contract](../../Contract/CA-Contract)

### Run CA

From the root of the repository go to the CA
```shell script
cd CA/
```

Define the following environment variables:

```shell script
export CA_URL=https://localhost:9090
```

Run the CA
```shell script
mvn clean install exec:java
```

### Setup CA.crt as trusted

Go to the resources of the CA
```shell script
cd src/main/resources
```

Create a new CA Certificate and a keystore
```shell script
./newCA.sh
```

Add CA Certificate to Java Trusted Certificates. _(You will need the password. (Default=changeit))_

```shell script
keytool -importcert -trustcacerts -cacerts -file SureReputeCA.crt -alias SureReputeCA -storepass changeit
```

Note: If you had previously done this step, then you need to delete first the LedgerCA certificate.

```shell script
keytool -delete -alias SureReputeCA -trustcacerts -cacerts -storepass changeit
```

## Build the CA Docker Image

### Image Prerequisites

- Docker >= 20.10.7
- Build the [CA-Contract](../../Contract/CA-Contract)
-

### Build Image

From the root of the module go to docker folder:

```shell script
cd docker
```

Build the image:

```shell script
./buildDockerImage.sh
```

## Run Locally with Docker Compose

### Docker Compose Prerequisites

- Docker >= 20.10.7
- [Build the CA Docker Image](#build-the-ca-docker-image)
- docker-compose >= 1.29.2

### Run Docker Compose

From the root of the module, run docker-compose containing both the SureRepute Server and SureRepute PSQL services _(Use the -d
parameter to detach)_:

```shell script
docker-compose up
```

Note: You can change the following environment variables in file [docker-compose.yml](docker/docker-compose.yml):

```shell script      
  - CA_URL=https://localhost:9090
```

Whenever you want to stop the services execute:

```shell script
docker-compose down
```

## Deploy to Google Cloud

### Google Cloud Prerequisites

- Docker >= 20.10.7 (or other Container/VM Manager)
- kubectl >= 1.22.2
- [Build the CA Docker Image](#build-the-ca-docker-image)

### Push the CA Docker Image to the GCloud Registry

Tag the image, with version 1.0.0:

```shell script
docker tag sure-repute-ca-server gcr.io/gsdsupport/sure-repute-ca-server:v1.0.0
```

Push to the registry:

```shell script
gcloud docker -- push gcr.io/gsdsupport/sure-repute-ca-server:v1.0.0
```

### Set up the Cluster and the Namespace
From the root of the project, change directory to k8s/sure-repute-namespace

```shell script
cd k8s/ca-namespace
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

Deploy the ca chart:

```shell script
 helm install ca ./ca-chart
```

To delete the identity provider chart we do:
```shell script
 helm uninstall surerepute
```

## Authors

| Name              | University                 |                                                                                                                                                                                                                                                                                                                                                             More info |
|:------------------|----------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
| Rafael Figueiredo | Instituto Superior Técnico |     [<img src="https://i.ibb.co/brG8fnX/mail-6.png" width="17">](mailto:rafafigoalexandre@gmail.com "rafafigoalexandre@gmail.com") [<img src="https://github.githubassets.com/favicon.ico" width="17">](https://github.com/rafafigo "rafafigo") [<img src="https://i.ibb.co/TvQPw7N/linkedin-logo.png" width="17">](https://www.linkedin.com/in/rafafigo/ "rafafigo") |