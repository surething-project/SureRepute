#!/bin/bash
### usage for adding: $key $value $filename
### usage for deleting: $key $filename

setProperty() {
  awk -v pat="^$1=" -v value="$1=$2" '{ if ($0 ~ pat) print value; else print $0; }' $3 > $3.tmp
  mv $3.tmp $3
}

getProperty() {
    grep "${1}" "${2}" | cut -d'=' -f2
}

removeProperty() {
  sed -i "/$1=./d" $2
}


if [ "$#" = 2 ]; then
    removeProperty "$1" "$2"
    exit
fi
key=$1
value=$2
filename=$3
current_value=$(getProperty "$key" "$filename")
if [ -z "$current_value" ]
then
      printf "%s\n" "$key=$value" >> "$filename"
else
      setProperty "$key" "$value" "$filename"
fi