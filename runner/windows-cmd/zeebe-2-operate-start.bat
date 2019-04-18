@echo off
TITLE Zeebe Operate

set mypath=%cd%
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

cd %operate%\bin
operate.bat

cd %mypath%	