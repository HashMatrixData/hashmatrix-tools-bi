#!/usr/bin/env bash
# 本地一键起栈 + 启动应用，验证 /actuator/health 通过。
# 用法：bash scripts/run-local.sh
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "▶ 启动本地依赖栈（Apache Doris all-in-one）"
docker compose -f docker-compose.local.yml up -d

echo "▶ 等待 Doris FE 就绪（首次自举较慢，最长约 5 分钟）..."
for i in $(seq 1 60); do
  if curl -sf http://localhost:8030/api/bootstrap >/dev/null 2>&1; then
    echo "  ✅ Doris FE 已就绪"
    break
  fi
  sleep 5
done

echo "▶ 构建并启动应用（local profile）"
mvn -B -ntp -DskipTests spring-boot:run -Dspring-boot.run.profiles=local
