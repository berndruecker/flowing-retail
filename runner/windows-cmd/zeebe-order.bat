@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Order
mvn package exec:java -f %flowing%\zeebe\java\order\