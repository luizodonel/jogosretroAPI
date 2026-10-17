@echo off
title RetroHub - Back-end Spring Boot
echo [RetroHub] Iniciando Back-end Spring Boot (Porta 8080)...
cd /d "%~dp0\backend"
mvn spring-boot:run
pause
