@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Inventory
mvn package exec:java -f %flowing%\zeebe\java\inventory\