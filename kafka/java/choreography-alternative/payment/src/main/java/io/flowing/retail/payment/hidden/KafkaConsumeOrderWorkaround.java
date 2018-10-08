package io.flowing.retail.payment.hidden;

import java.util.Collections;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Workaround to consume all records via consumer group
 * "order".
 * 
 * Why? 
 * 
 * Because this consumer group is not active in the choreographed example
 * so when switching to the orchestrated example we would still consume
 * "old" messages in the order component. While this is not necessarily a problem
 * I prefer to avoid it to save time to explain this in demos. Also Zeebe in the 
 * current alpha has problems with messages with a payload that does not fit
 * its IO mapping.
 * 
 * So this class simply consumes all messages in the consumer group.
 */
@Component
public class KafkaConsumeOrderWorkaround {
  
  @Value("${spring.cloud.stream.kafka.binder.brokers}")
  private String bootstrapServers;  

  @Value("${spring.cloud.stream.bindings.input.destination}")
  private String topicName;  

  private String consumerGroup = "order";

  private Consumer<Long, String> consumer;

  private long pollingInterval = 250;
  private boolean running = true;

  private Thread consumerThread;

  @PostConstruct
  public void startConsuming() {
    consumerThread = new Thread("kafka-workaround-consumer") {
      public void run( ) {
        
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroup);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topicName));
        while (running) {
          consumer.poll(pollingInterval);
          consumer.commitAsync();
        }        
        consumer.close();
      }
    };
    consumerThread.start();
  }
  
  @PreDestroy
  public void stopConsuming() {
    running = false;
  }
}
