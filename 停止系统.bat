@echo off
setlocal
pushd "%~dp0" || exit /b 1
set "docker=docker.exe"
if exist "%ProgramFiles%\Docker\Docker\resources\bin\docker.exe" set "docker=%ProgramFiles%\Docker\Docker\resources\bin\docker.exe"
"%docker%" compose down
set "code=%errorlevel%"
popd
if not "%code%"=="0" pause
exit /b %code%
