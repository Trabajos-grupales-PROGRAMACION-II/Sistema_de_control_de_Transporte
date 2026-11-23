@echo off
chcp 65001 > nul
title Sistema de Gestion y Monitoreo de Transporte
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar "%~dp0SistemaTransporte.jar"
pause
