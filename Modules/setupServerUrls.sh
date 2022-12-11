#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <nr of servers>" >&2
  echo "Add data.json with the appropriate information"
  exit 1
fi

chmod +x setProp.sh
>SureRepute-Server/src/main/resources/url.properties
>IdentityProvider/src/main/resources/url.properties

for ((i = 0; i < $1; ++i)); do
  id=$(jq -r ".servers[$i][0]" data.json)
  cs=$(jq -r ".servers[$i][1]" data.json)
  ss=$(jq -r ".servers[$i][2]" data.json)
  sip=$(jq -r ".servers[$i][3]" data.json)
  if [ -z "$id" ] || [ -z "$cs" ] || [ -z "$ss" ] || [ -z "$sip" ]; then
    echo "Add data.json with the appropriate information"
    exit
  fi
  ./setProp.sh "$id" "$ss/v1" SureRepute-Server/src/main/resources/url.properties
  ./setProp.sh "$id" "$sip/v1" IdentityProvider/src/main/resources/url.properties
done