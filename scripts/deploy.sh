#!/usr/bin/env bash
set -euo pipefail

: "${IMAGE:?IMAGE is required}"
: "${GHCR_USERNAME:?GHCR_USERNAME is required}"
: "${GHCR_TOKEN:?GHCR_TOKEN is required}"

CONTAINER_NAME="${CONTAINER_NAME:-spring-app}"
SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"
ENV_FILE="${ENV_FILE:-/etc/spring-app/app.env}"

if [ ! -f "$ENV_FILE" ]; then
  echo "ENV_FILE not found: $ENV_FILE"
  exit 1
fi

echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin

docker pull "$IMAGE"

docker stop "$CONTAINER_NAME" || true
docker rm "$CONTAINER_NAME" || true

docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  --network host \
  --env-file "$ENV_FILE" \
  -e "SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE" \
  "$IMAGE"

docker image prune -f

docker ps --filter "name=$CONTAINER_NAME"