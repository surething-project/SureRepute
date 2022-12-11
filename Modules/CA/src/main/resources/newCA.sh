#!/bin/sh
# Generates a New Private Key
openssl genrsa -out SureReputeCA.key 4096

# Generates a New Self Signing Certificate
openssl req -new -x509 -key SureReputeCA.key -sha256 -subj "/CN=localhost/O=surething/OU=SureReputeCA/C=PT" -addext 'subjectAltName=DNS:ca.surething-surerepute.com' -days 3650 -out SureReputeCA.crt

# Generates a keystore
openssl pkcs12 -export -in SureReputeCA.crt -name SureReputeCACert -inkey SureReputeCA.key -out SureReputeCA.p12 -passout "pass:SureReputeCA"

# Cleanup
rm SureReputeCA.key

