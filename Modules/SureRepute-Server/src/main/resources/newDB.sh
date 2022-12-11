#!/bin/sh

DB_NAME=$(echo "$1" | tr '[:upper:]' '[:lower:]')
DB_USER=$(echo "$2" | tr '[:upper:]' '[:lower:]')
DB_PWD=$(echo "$3" | tr '[:upper:]' '[:lower:]')
DB_SCHEMA="schema.sql"

CMD="
dropdb --if-exists ${DB_NAME};
createdb ${DB_NAME};
echo \"
  DROP ROLE IF EXISTS ${DB_USER};
  CREATE ROLE ${DB_USER} LOGIN SUPERUSER PASSWORD '${DB_PWD}';
\" | psql ${DB_NAME};
"

sudo service postgresql start
echo "${CMD}" | sudo su -l postgres

psql -d ${DB_NAME} -f ${DB_SCHEMA}