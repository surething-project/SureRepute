#!/bin/sh

cd CA-Contract/doc
./gen-openapi.bash
cd ../../IdentityProvider-Contract/doc
./gen-openapi.bash
cd ../../SureRepute-CS-Contract/doc
./gen-openapi.bash
cd ../../SureRepute-SS-Contract/doc
./gen-openapi.bash
cd ../../SureRepute-SIP-Contract/doc
./gen-openapi.bash
