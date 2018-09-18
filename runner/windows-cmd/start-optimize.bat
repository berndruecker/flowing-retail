@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

set mypath=%cd%
cd %optimize%

start-optimize.bat

cd %mypath%	