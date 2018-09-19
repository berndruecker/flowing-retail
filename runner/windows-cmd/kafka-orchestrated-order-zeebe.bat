@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Order (Zeebe)
mvn package exec:java -f %flowing%\kafka\java\order-zeebe\