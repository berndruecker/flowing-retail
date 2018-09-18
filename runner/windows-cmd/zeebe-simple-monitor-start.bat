@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

TITLE Zeebe Simple Monitor
sc start MongoDB

java -jar %simplemonitor%