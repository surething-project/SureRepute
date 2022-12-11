#!/bin/bash
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <nr of servers>" >&2
  exit 1
fi

cd Contract || exit
chmod +x buildAll.sh
./buildAll.sh
cd - || exit

cd Modules || exit

if [ ! -f "CA/src/main/resources/SureReputeCA.crt" ]; then
   find . -name "*.crt" -type f -delete
   find . -name "*.p12" -type f -delete
   cd CA/src/main/resources || exit
   chmod +x newCA.sh
   sh newCA.sh
   keytool -delete -alias SureReputeCA -trustcacerts -cacerts -storepass changeit
   yes | keytool -importcert -trustcacerts -cacerts -file SureReputeCA.crt -alias SureReputeCA -storepass changeit
   cd - || exit
fi

cd IdentityProvider/src/main/resources || exit
chmod +x newDB.sh
./newDB.sh "IdentityProvider" "IdentityProvider" "IdentityProvider"
cd - || exit

for ((i = 0; i < $1; ++i)); do
  id=$(jq -r ".servers[$i][0]" data.json)
  if [ -z "$id" ]; then
    echo "Add data.json with the appropriate information"
    exit
  fi
  cd SureRepute-Server/src/main/resources || exit
  chmod +x newDB.sh
  ./newDB.sh "$id" "$id" "$id"
  cd - || exit
done
