@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Payment (chorographed)
mvn exec:java -f %flowing%\kafka\java\choreography-alternative\payment\