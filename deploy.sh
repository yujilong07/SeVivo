#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_dir"

docker compose up --build -d
docker compose ps

echo "API is available at http://localhost:${APP_PORT:-8080}"
