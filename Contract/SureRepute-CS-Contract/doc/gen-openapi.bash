#!/bin/bash

protoc ../src/main/proto/SureRepute-CS.proto -I../src/main/proto -I../../Google --openapi_out=.

sed -i 's/application\/json/application\/x-protobuf/g' ./openapi.yaml

python3 swagger-yaml-to-html.py <./openapi.yaml >openapi.html