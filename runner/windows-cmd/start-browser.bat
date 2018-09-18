@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
start "" file:///%flowing%\docs\overview-website\index.html