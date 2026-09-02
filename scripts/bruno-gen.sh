#!/usr/bin/env bash
set -euo pipefail

COLLECTION_NAME="saasy-core"
COLLECTION_DIR="bruno/${COLLECTION_NAME}"
OPENAPI_SPEC="api/openapi.yaml"
BACKUP_DIR=$(mktemp -d)

# Ensure bruno/ parent directory exists
mkdir -p bruno

# Bootstrap case: collection doesn't exist yet
if [[ ! -d "$COLLECTION_DIR" ]]; then
  echo "Bootstrapping collection..."
  bru import openapi \
    --source "$OPENAPI_SPEC" \
    --output "$COLLECTION_DIR" \
    --collection-name "$COLLECTION_NAME"
  mkdir -p "$COLLECTION_DIR/environments"
  echo "Created $COLLECTION_DIR — add environment files manually"
  exit 0
fi

# Existing collection: backup, regenerate, restore
echo "Backing up environments and collection settings..."
cp -r "$COLLECTION_DIR/environments" "$BACKUP_DIR/" 2>/dev/null || true
cp "$COLLECTION_DIR/bruno.json" "$BACKUP_DIR/" 2>/dev/null || true
cp "$COLLECTION_DIR/collection.bru" "$BACKUP_DIR/" 2>/dev/null || true
cp "$COLLECTION_DIR/.env" "$BACKUP_DIR/" 2>/dev/null || true

# Regenerate from OpenAPI
echo "Importing OpenAPI spec..."
rm -rf "$COLLECTION_DIR"
bru import openapi \
  --source "$OPENAPI_SPEC" \
  --output "$COLLECTION_DIR" \
  --collection-name "$COLLECTION_NAME"

# Restore environments, bruno.json, collection.bru, and secrets
echo "Restoring environments and collection settings..."
mkdir -p "$COLLECTION_DIR/environments"
cp -r "$BACKUP_DIR/environments/"* "$COLLECTION_DIR/environments/" 2>/dev/null || true
cp "$BACKUP_DIR/bruno.json" "$COLLECTION_DIR/" 2>/dev/null || true
cp "$BACKUP_DIR/collection.bru" "$COLLECTION_DIR/" 2>/dev/null || true
cp "$BACKUP_DIR/.env" "$COLLECTION_DIR/" 2>/dev/null || true

# Remove auto-generated default environment (we have our own)
rm -f "$COLLECTION_DIR/environments/Environment 1.bru"

# Cleanup
rm -rf "$BACKUP_DIR"

echo "Done: $COLLECTION_DIR generated"
