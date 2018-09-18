@echo off
TITLE Zeebe
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

set mypath=%cd%
cd %kafka%\bin\windows\

REM kafka-topics.bat --delete --zookeeper localhost:2181 --topic flowing-retail
REM kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic flowing-retail

kafka-console-consumer.bat --zookeeper localhost:2181 --topic flowing-retail --consumer-property group.id=payment
kafka-console-consumer.bat --zookeeper localhost:2181 --topic flowing-retail --consumer-property group.id=shipping
kafka-console-consumer.bat --zookeeper localhost:2181 --topic flowing-retail --consumer-property group.id=inventory

cd %mypath%	