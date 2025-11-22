@echo off
set JLINK_VM_OPTIONS=--strip-debug --compress=2 --no-header-files --no-man-pages
set DIR=%~dp0
"%DIR%\java" %JLINK_VM_OPTIONS% -m ciencias/ciencias.App %*
