@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1" %*
set "code=%errorlevel%"
if not "%code%"=="0" pause
exit /b %code%
