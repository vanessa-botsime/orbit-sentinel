#!/usr/bin/env bash
# Generates events in Orbit so Sentinel has something to detect.
# Needs: Orbit on :8080, Sentinel on :8081, curl.
set -euo pipefail

ORBIT=${ORBIT_URL:-http://localhost:8080}
SENTINEL=${SENTINEL_URL:-http://localhost:8081}
EMAIL=${DEMO_EMAIL:-demo@example.com}
PASSWORD=${DEMO_PASSWORD:-a-long-demo-password}

json() { printf '{"email":"%s","password":"%s"}' "$1" "$2"; }
post() { curl -s -X POST "$1" -H 'Content-Type: application/json' "${@:2}"; }

echo "1. Register the demo user (201 = created, 409 = already exists)"
curl -s -o /dev/null -w "   HTTP %{http_code}\n" -X POST "$ORBIT/api/auth/register" \
  -H 'Content-Type: application/json' -d "$(json "$EMAIL" "$PASSWORD")"

echo "2. Six failed logins (should trigger FAILED_LOGIN_BURST)"
for i in 1 2 3 4 5 6; do
  curl -s -o /dev/null -w "   HTTP %{http_code}" -X POST "$ORBIT/api/auth/login" \
    -H 'Content-Type: application/json' -d "$(json "$EMAIL" "wrong-password-$i")"
done
echo

echo "3. Successful login"
TOKEN=$(post "$ORBIT/api/auth/login" -d "$(json "$EMAIL" "$PASSWORD")" | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "   got a token (${#TOKEN} characters)"

echo "4. Create an application and move it to INTERVIEW"
ID=$(post "$ORBIT/api/applications" -H "Authorization: Bearer $TOKEN" \
  -d '{"company":"Demo Corp","role":"Graduate Developer"}' | sed -E 's/.*"id":([0-9]+).*/\1/')
curl -s -o /dev/null -w "   HTTP %{http_code}\n" -X PATCH "$ORBIT/api/applications/$ID/status" \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"status":"INTERVIEW"}'

echo
echo "Done. Wait a few seconds, then read Sentinel with an ADMIN or AUDITOR token:"
echo "  curl -H 'Authorization: Bearer <token>' $SENTINEL/api/alerts"
echo "  curl -H 'Authorization: Bearer <token>' $SENTINEL/api/audit-events"
echo "  curl -H 'Authorization: Bearer <admin-token>' $SENTINEL/api/audit-events/verify"
