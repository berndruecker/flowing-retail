FROM confluentinc/cp-kafka:latest

ADD kafka-connect/connect-standalone.properties kafka-connect/zeebe-connector-sink.properties kafka-connect/zeebe-connector-source.properties /etc/kafka-connect/
ADD kafka-connect/*.jar /etc/kafka-connect/jars/

ENTRYPOINT exec /usr/bin/connect-standalone /etc/kafka-connect/connect-standalone.properties /etc/kafka-connect/zeebe-connector-sink.properties /etc/kafka-connect/zeebe-connector-source.properties