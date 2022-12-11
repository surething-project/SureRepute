#!/bin/sh

if [ "$1" = "create" ]; then
  # Create Cluster
  gcloud container clusters create "sure-repute-gke-regional" \
    --region "europe-west1" --machine-type "e2-standard-4" --disk-type \
    "pd-standard" --disk-size "20" --num-nodes "2" --node-locations \
    "europe-west1-b"

  # Create and Apply the External DNS
  kubectl apply -f external-dns.yaml

  # Create and Apply the Nginx Ingress Controller
  kubectl apply -f nginx-ingress.yaml
  echo "Waiting for the NGINX ingress controller to be created"
  sleep 60
elif [ "$1" = "delete" ]; then
  gcloud container clusters delete "sure-repute-gke-regional" \
    --region "europe-west1"
  exit 1
fi

gcloud container clusters get-credentials sure-repute-gke-regional --region europe-west1

# Elevate to the Cluster Admin Role
kubectl create clusterrolebinding cluster-admin-binding \
  --clusterrole=cluster-admin --user="$(gcloud config get-value core/account)"

# Create Namespace
kubectl apply -f ca-namespace.yaml
