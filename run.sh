#!/bin/bash

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <nr of servers>" >&2
  exit 1
fi

cd Modules || exit

ca=$(jq -r '.ca' data.json)
ip=$(jq -r '.identityProvider' data.json)

gnome-terminal --tab -- /bin/bash -c "
cd CA;
./run.sh $ca;
bash"

chmod +x setupServerUrls.sh
./setupServerUrls.sh "$1"

sleep 5

for ((i = 0; i < $1; i++)); do
  id=$(jq -r ".servers[$i][0]" data.json)
  cs=$(jq -r ".servers[$i][1]" data.json)
  ss=$(jq -r ".servers[$i][2]" data.json)
  sip=$(jq -r ".servers[$i][3]" data.json)
  gnome-terminal --tab -- /bin/bash -c "
      cd SureRepute-Server;
      ./run.sh '$id' '$cs' '$ss' '$sip';
      bash"
  sleep 7
done

gnome-terminal --tab -- /bin/bash -c "
cd IdentityProvider;
./run.sh IdentityProvider '$ip';
bash"
