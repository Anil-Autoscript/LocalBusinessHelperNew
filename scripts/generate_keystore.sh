#!/bin/bash
# ============================================================
# generate_keystore.sh
# Run this ONCE locally to create your release signing keystore
# Then encode it as base64 and store in GitHub Secrets
# ============================================================

set -e

KEYSTORE_FILE="release.keystore"
KEY_ALIAS="local-business-helper"
KEY_PASSWORD="your_secure_password_here"
STORE_PASSWORD="your_secure_store_password_here"
VALIDITY_DAYS=10000

echo "========================================="
echo "  Local Business Helper Keystore Setup"
echo "========================================="
echo ""

# Generate the keystore
keytool -genkey -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity $VALIDITY_DAYS \
  -storepass "$STORE_PASSWORD" \
  -keypass "$KEY_PASSWORD" \
  -dname "CN=Local Business Helper, OU=Development, O=YourCompany, L=Mumbai, S=Maharashtra, C=IN"

echo ""
echo "✅ Keystore generated: $KEYSTORE_FILE"
echo ""

# Base64 encode for GitHub Secrets
BASE64_KEYSTORE=$(base64 -w 0 "$KEYSTORE_FILE")

echo "========================================="
echo " Add these to GitHub Secrets:"
echo " (Settings → Secrets → Actions)"
echo "========================================="
echo ""
echo "Secret Name: KEYSTORE_BASE64"
echo "Secret Value:"
echo "$BASE64_KEYSTORE"
echo ""
echo "Secret Name: KEYSTORE_PASSWORD"
echo "Secret Value: $STORE_PASSWORD"
echo ""
echo "Secret Name: KEY_ALIAS"
echo "Secret Value: $KEY_ALIAS"
echo ""
echo "Secret Name: KEY_PASSWORD"
echo "Secret Value: $KEY_PASSWORD"
echo ""
echo "========================================="
echo " IMPORTANT: Keep release.keystore safe!"
echo " Add it to .gitignore — NEVER commit it!"
echo "========================================="
