@echo off
TITLE ElasticsearchForZeebe

set mypath=%cd%
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

cd %elasticsearch%\bin
elasticsearch.bat

cd %mypath%	