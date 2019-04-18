@echo off
TITLE Zeebe

set mypath=%cd%
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

set SLEEP_TIME=10
start "Zeebe Operate Elasticsearch" call %elasticsearch%\bin\elasticsearch.bat
echo "Waiting for Elasticsearch to come up..."
timeout /t %SLEEP_TIME% /nobreak >nul

cd %zeebe%\bin
broker.bat

cd %mypath%	