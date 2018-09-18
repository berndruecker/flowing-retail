@echo off
TITLE Zeebe
FOR /F "tokens=1,2 delims==" %%G IN (environment.properties) DO (set %%G=%%H)

set mypath=%cd%
cd %kafka%\bin\windows\

REM kafka-topics.bat --delete --zookeeper localhost:2181 --topic flowing-retail
REM kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic flowing-retail

call kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic flowing-retail --timeout-ms=10 --from-beginning --consumer-property group.id=payment 
call kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic flowing-retail --timeout-ms=10 --from-beginning --consumer-property group.id=shipping 
call kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic flowing-retail --timeout-ms=10 --from-beginning --consumer-property group.id=inventory 
call kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic flowing-retail --timeout-ms=10 --from-beginning --consumer-property group.id=order

cd %mypath%	