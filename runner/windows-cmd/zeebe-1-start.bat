@echo off
TITLE Zeebe

set mypath=%cd%
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
cd %zeebe%\bin
broker.bat
cd %mypath%	