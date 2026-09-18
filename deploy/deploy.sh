#!/usr/bin/env bash
# ============================================================
# 一键部署脚本（在服务器上执行）
# 用法： sudo bash deploy/deploy.sh
# 前置：已完成一次「初见部署」（见 开发文档/7.部署上线方案.md），
#       /opt/jdk-21、/opt/interview/{back,front}、systemd 单元、nginx 都已就绪。
# ============================================================
set -euo pipefail

APP_DIR=/opt/interview
REPO_DIR=${REPO_DIR:-/opt/interview/repo}
JDK_HOME=${JDK_HOME:-/opt/jdk-21}
SERVICE=interview-back

echo "==> 1/4 拉取代码"
cd "$REPO_DIR"
git pull --ff-only || echo "（非 git 目录或拉取失败，改用当前代码继续）"

echo "==> 2/4 构建后端"
cd "$REPO_DIR/back/InterViewBacked"
JAVA_HOME="$JDK_HOME" ./mvnw -q -DskipTests clean package
cp -f target/*.jar "$APP_DIR/back/interview-backed.jar"

echo "==> 3/4 构建前端"
cd "$REPO_DIR/front/InterviewProjectProject"
npm ci
npm run build
rm -rf "$APP_DIR/front"
cp -r dist "$APP_DIR/front"

echo "==> 4/4 重启后端并校验"
systemctl restart "$SERVICE"
sleep 8
systemctl is-active --quiet "$SERVICE" && echo "后端已启动" || { journalctl -u "$SERVICE" -n 50 --no-pager; exit 1; }
curl -fsS http://127.0.0.1:8080/api/notes?scope=plaza > /dev/null && echo "接口自检通过"

echo "完成：如有前端改动，nginx 直接读磁盘新文件，无需 reload。"
