if [ "$#" -ne 2 ]; then
  echo "Usage: $0 <id> <identity_provider_url>" >&2
  exit 1
fi

export IDENTITY_PROVIDER_URL=$2
export DB_CONNECTION=localhost
export DB_PORT=5432
export DB_NAME=$(echo "$1" | tr '[:upper:]' '[:lower:]')
export DB_USER=$(echo "$1" | tr '[:upper:]' '[:lower:]')
export DB_PWD=$(echo "$1" | tr '[:upper:]' '[:lower:]')

mvn clean compile exec:java
