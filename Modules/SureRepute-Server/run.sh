if [ "$#" -ne 4 ]; then
  echo "Usage: $0 <id> <client_server_url> <server_server_url> <ip_server_url>" >&2
  exit 1
fi

export ID=$1
export CLIENT_SERVER_URL=$2
export SERVER_SERVER_URL=$3
export IP_SERVER_URL=$4
export DB_CONNECTION=localhost
export DB_PORT=5432
export DB_NAME=$(echo "$1" | tr '[:upper:]' '[:lower:]')
export DB_USER=$(echo "$1" | tr '[:upper:]' '[:lower:]')
export DB_PWD=$(echo "$1" | tr '[:upper:]' '[:lower:]')

mvn clean compile exec:java
