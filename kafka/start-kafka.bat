@RD /S /Q C:\tmp\kafka-logs >nul 2>&1
@RD /S /Q C:\tmp\zookeeper >nul 2>&1

set mypath=%cd%

cd c:\flowing.io\Kafka\kafka_2.11-1.0.1\bin\windows\
Start zookeeper-server-start ../../config/zookeeper.properties
Start kafka-server-start ../../config/server.properties

# wait for 5 seconds
ping 127.0.0.1 -n 6 > nul

kafka-topics.bat --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic flowing-retail

cd %mypath%