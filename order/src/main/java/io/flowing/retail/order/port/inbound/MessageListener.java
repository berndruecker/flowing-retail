package io.flowing.retail.order.port.inbound;

import java.io.IOException;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.port.Message;
import io.flowing.retail.order.repository.OrderRepository;

@Component
@EnableBinding(Sink.class)
public class MessageListener {
  
  @Autowired
  private OrderRepository repository;
  
  @Autowired
  private ProcessEngine camunda;
    
  /**
   * Handles incoming OrderPlacedEvents. 
   * 
   *  Using the conditional {@link StreamListener} from 
   * https://github.com/spring-cloud/spring-cloud-stream/blob/master/spring-cloud-stream-core-docs/src/main/asciidoc/spring-cloud-stream-overview.adoc
   * in a way close to what Axion
   *  would do (see e.g. https://dturanski.wordpress.com/2017/03/26/spring-cloud-stream-for-event-driven-architectures/)
   */
  @StreamListener(target = Sink.INPUT, 
      condition="payload.messageType.toString()=='OrderPlacedEvent'")
  @Transactional
  public void orderPlacedReceived(String messageJson) throws JsonParseException, JsonMappingException, IOException {
    Message<Order> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<Order>>(){});
    Order order = message.getPayload();
    System.out.println(order);
    
    repository.persistOrder(order);    
    
    camunda.getRuntimeService().createMessageCorrelation(message.getMessageType())
      .processInstanceBusinessKey(message.getTraceId()) // TODO: double check
      .setVariable("orderId", order.getId())
      .correlateWithResult();
  }
  
  /**
   * Very generic listener for simplicity. It might make very much sense to
   * handle each and every message type individually.
   */
  @StreamListener(target = Sink.INPUT, 
      condition="payload.messageType.toString().endsWith('Event')")
  @Transactional
  public void messageReceived(Message<String> message) {
    System.out.println(message);
    
    long correlatingInstances = camunda.getRuntimeService().createExecutionQuery() //
      .messageEventSubscriptionName(message.getMessageType()) //
      .processInstanceBusinessKey(message.getTraceId()) //
      .count();
    
    if (correlatingInstances==1) {    
      camunda.getRuntimeService().createMessageCorrelation(message.getMessageType())
        .processInstanceBusinessKey(message.getTraceId())
        .setVariable("RESPONSE_" + message.getMessageType(), message.getPayload())
        .correlateWithResult();
    } else {
      // ignoring event, not interested
      System.out.println("Order ignores " + message.getMessageType());
    }
    
  }  
  
//  @StreamListener(target = Sink.INPUT)
//  public void handleDefaultEvent(@Payload String payload) {
//    System.out.println("Received payload: " + payload);
//  }
}
