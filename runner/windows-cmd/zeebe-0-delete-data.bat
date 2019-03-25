@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
@RD /S /Q %zeebe%\data >nul 2>&1

DEL %HOMEPATH%\zeebe-monitor.trace.db >nul 2>&1
DEL %HOMEPATH%\zeebe-monitor.mv.db >nul 2>&1

 