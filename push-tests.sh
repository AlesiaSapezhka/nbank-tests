#!/bin/bash

DOCKERHUB_USERNAME="alesyasapezhko"
IMAGE_NAME="nbank-tests"
TAG="latest"

echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

docker tag "$IMAGE_NAME:$TAG" "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

docker push "$DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"

echo "Image pushed successfully!"
echo "To pull the image, run:"
echo "docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"