@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
@RD /S /Q %kafka-data%\kafka-logs2 >nul 2>&1
@RD /S /Q %kafka-data%\zookeeper2 >nul 2>&1

 