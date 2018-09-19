@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Zeebe Deploy
copy %flowing%\kafka\java\choreography-alternative\zeebe-track\order-tracking.bpmn %flowing%\kafka\java\choreography-alternative\zeebe-track\src\main\resources
mvn package exec:java -f %flowing%\kafka\java\choreography-alternative\zeebe-track\