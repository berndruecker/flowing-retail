@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Kafka Connect

set mypath=%cd%
cd %kafka%\bin\windows\

xcopy %flowing%\kafka\java\choreography-alternative\zeebe-track\kafka-connect\*.jar %kafka-data%\plugins\ /E /I /Y

connect-standalone %kafka%/config/connect-standalone.properties %flowing%/kafka/java/choreography-alternative/zeebe-track/kafka-connect/zeebe-connector-sink.properties

cd %mypath%	