@echo off
REM ============================================================
REM Local dev starter: backend (Spring Boot :8080) + frontend (Vite :5180)
REM Usage: double-click this file, or run: scripts\start-dev.bat
REM NOTE: keep this file ASCII-only (no Chinese chars) to avoid encoding issues.
REM ============================================================

set PROJECT_ROOT=%~dp0..
set JAVA_HOME=C:\Program Files\Java\jdk-21

echo Starting backend on :8080 ...
start "interview-backend" cmd /k "cd /d %PROJECT_ROOT%\back\InterViewBacked && mvnw.cmd -DskipTests spring-boot:run -Dspring-boot.run.arguments=--server.port=8080"

echo Waiting for backend to warm up (25s)...
timeout /t 25 /nobreak > nul

echo Starting frontend on :5180 ...
start "interview-frontend" cmd /k "cd /d %PROJECT_ROOT%\front\InterviewProjectProject && npm run dev"

timeout /t 8 /nobreak > nul
start "" http://localhost:5180

echo.
echo Backend :8080  Frontend :5180
echo If the frontend says "port 5180 is already in use", close the old Vite window and retry.
echo Demo account: admin / 123456
