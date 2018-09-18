@echo off
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)
TITLE Kafka Connect

set mypath=%cd%
cd %kafka%\bin\windows\

connect-standalone ../../config/connect-standalone.properties C:/DEV/zeebe/develop/Kafka/kafka_2.11-1.0.1/config/zeebe-connector.properties C:/DEV/zeebe/develop/Kafka/kafka_2.11-1.0.1/config/zeebe-connector2.properties

cd %mypath%	