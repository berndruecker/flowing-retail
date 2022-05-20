package io.flowing.retail.kafka.order.messages;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.camunda.zeebe.client.ZeebeClient;
import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.process.OrderFlowContext;
import io.flowing.retail.kafka.order.process.payload.GoodsFetchedEventPayload;
import io.flowing.retail.kafka.order.process.payload.GoodsShippedEventPayload;
import io.flowing.retail.kafka.order.process.payload.PaymentReceivedEventPayload;
import io.flowing.retail.kafka.order.persistence.OrderRepository;

@Component
public class MessageListener {

  @Autowired
  private OrderRepository repository;

  @Autowired
  private ZeebeClient zeebe;
	
  @Autowired
  private ObjectMapper objectMapper;  

  @KafkaListener(id = "order", topics = MessageSender.TOPIC_NAME)
  public void messageReceived(String messagePayloadJson, @Header("type") String messageType) throws Exception{
    if ("OrderPlacedEvent".equals(messageType)) {
      orderPlacedReceived(objectMapper.readValue(messagePayloadJson, new TypeReference<Message<Order>>() {}));
    }
    if ("PaymentReceivedEvent".equals(messageType)) {
      paymentReceived(objectMapper.readValue(messagePayloadJson, new TypeReference<Message<PaymentReceivedEventPayload>>() {}));
    }
    else if ("GoodsFetchedEvent".equals(messageType)) {
      goodsFetchedReceived(objectMapper.readValue(messagePayloadJson, new TypeReference<Message<GoodsFetchedEventPayload>>() {}));
    }
    else if ("GoodsShippedEvent".equals(messageType)) {
      goodsShippedReceived(objectMapper.readValue(messagePayloadJson, new TypeReference<Message<GoodsShippedEventPayload>>() {}));
    }
    else {
      System.out.println("Ignored message of type " + messageType );
    }
  }

  @Transactional
  public void orderPlacedReceived(Message<Order> message) throws JsonParseException, JsonMappingException, IOException {
    Order order = message.getData();
    
    // persist domain entity
    // (if we want to do this "transactional" this could be a step in the workflow)
    repository.save(order);

    // prepare data for workflow
    OrderFlowContext context = new OrderFlowContext();
    context.setOrderId(order.getId());
    context.setTraceId(message.getTraceid());

    // and kick of a new flow instance
    System.out.println("New order placed, start flow with " + context);
    zeebe.newCreateInstanceCommand() //
        .bpmnProcessId("order-kafka") //
        .latestVersion() // 
        .variables(context.asMap()) //
        .send().join();
  }

  @Transactional
  public void paymentReceived(Message<PaymentReceivedEventPayload> message) throws Exception {
     // Here you would maybe we should read something from the payload:
    message.getData();

    zeebe.newPublishMessageCommand() //
      .messageName(message.getType())
      .correlationKey(message.getCorrelationid())
      .variables(Collections.singletonMap("paymentInfo", "YeahWeCouldAddSomething"))
      .send().join();  

    System.out.println("Correlated " + message);
  }

  @Transactional
  public void goodsFetchedReceived(Message<GoodsFetchedEventPayload> message) throws Exception {
    String pickId = message.getData().getPickId();     

    zeebe.newPublishMessageCommand() //
        .messageName(message.getType()) //
        .correlationKey(message.getCorrelationid()) // 
        .variables(Collections.singletonMap("pickId", pickId)) //
        .send().join();

    System.out.println("Correlated " + message );
  }


  @Transactional
  public void goodsShippedReceived(Message<GoodsShippedEventPayload> message) throws Exception {
    String shipmentId = message.getData().getShipmentId();     

    zeebe.newPublishMessageCommand() //
        .messageName(message.getType()) //
        .correlationKey(message.getCorrelationid()) //
        .variables(Collections.singletonMap("shipmentId", shipmentId)) //
        .send().join();

    System.out.println("Correlated " + message );
  }
}
