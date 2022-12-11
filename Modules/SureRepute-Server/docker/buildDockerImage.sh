#!/bin/sh
mkdir local-maven-repo
docker image rm sure-repute-server
docker image rmi gcr.io/gsdsupport/sure-repute-server:v1.0.0

# Copy CA-Key
cp ${JAVA_HOME}/lib/security/cacerts cacerts

ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/SureRepute-SS-Contract/1.0-SNAPSHOT/SureRepute-SS-Contract-1.0-SNAPSHOT.jar ss.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/SureRepute-CS-Contract/1.0-SNAPSHOT/SureRepute-CS-Contract-1.0-SNAPSHOT.jar cs.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/SureRepute-SIP-Contract/1.0-SNAPSHOT/SureRepute-SIP-Contract-1.0-SNAPSHOT.jar sip.jar
ln -s ~/.m2/repository/pt/ulisboa/tecnico/surerepute/CA-Contract/1.0-SNAPSHOT/CA-Contract-1.0-SNAPSHOT.jar ca.jar

mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=SureRepute-SS-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=ss.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=SureRepute-CS-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=cs.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=SureRepute-SIP-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=sip.jar -Dpackaging=jar
mvn deploy:deploy-file -DgroupId=pt.ulisboa.tecnico.surerepute -DartifactId=CA-Contract -Dversion=1.0-SNAPSHOT -Durl=file:./local-maven-repo/ -DrepositoryId=local-maven-repo -DupdateReleaseInfo=true -Dfile=ca.jar -Dpackaging=jar

docker build -t sure-repute-server ../ -f Dockerfile

rm cacerts
rm *.jar