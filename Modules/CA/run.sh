if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <ca_url>" >&2
  exit 1
fi

export CA_URL=$1
mvn clean compile exec:java