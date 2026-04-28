#!/usr/bin/env bash
# Tráfego sintético: GET /policies pelos 5 CPFs seed em loop.
#
#   make load
#   HOST=http://10.0.0.5:8083 ./scripts/load.sh
#
# Pré-requisitos: app rodando (`make run`).

set -u

HOST="${HOST:-http://localhost:8083}"
DELAY_MS="${DELAY_MS:-100}"

CPFS=("12345678901" "98765432109" "11122233344" "55566677788" "10010010055")

list_policies() {
    local cpf=$1
    local page=$2
    local size=$3
    curl -s -o /dev/null -w "%{http_code}\n" \
        "$HOST/open-insurance/insurance-policies/v1/policies?documentType=CPF&document=$cpf&page=$page&page-size=$size" \
        || true
}

list_and_get_one() {
    local cpf=$1
    local body=$(curl -s "$HOST/open-insurance/insurance-policies/v1/policies?documentType=CPF&document=$cpf&page-size=5")
    local pid=$(echo "$body" | grep -oE '"policyId":"[^"]+"' | head -1 | sed 's/"policyId":"//;s/"$//')
    if [ -n "$pid" ]; then
        curl -s -o /dev/null "$HOST/open-insurance/insurance-policies/v1/policies/$pid" || true
    fi
}

trap 'echo; echo "stopping load"; exit 0' INT TERM

echo "loading $HOST (delay=${DELAY_MS}ms). Ctrl+C to stop."
i=0
while true; do
    cpf="${CPFS[$((i % 5))]}"
    list_policies "$cpf" 1 10 >/dev/null
    list_and_get_one "$cpf"
    sleep "0.$((DELAY_MS / 100))"
    i=$((i + 1))
done
