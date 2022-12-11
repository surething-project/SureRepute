#!/bin/sh

cd CA-Contract
mvn clean install
cd ../IdentityProvider-Contract
mvn clean install
cd ../SureRepute-CS-Contract
mvn clean install
cd ../SureRepute-SS-Contract
mvn clean install
cd ../SureRepute-SIP-Contract
mvn clean install