#!/bin/sh
mkdir local-maven-repo
docker image rm sure-repute-ca-server
docker image rmi gcr.io/gsdsupport/sure-repute-ca-server:v1.0.0

ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/CA-Contract/1.0-SNAPSHOT/CA-Contract-1.0-SNAPSHOT.jar ca.jar

mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=CA-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=ca.jar -Dpackaging=jar

docker build -t sure-repute-ca-server ../ -f Dockerfile

rm *.jar