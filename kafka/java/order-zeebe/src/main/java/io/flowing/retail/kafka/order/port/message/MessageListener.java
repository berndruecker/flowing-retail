package io.flowing.retail.kafka.order.port.message;

import java.io.IOException;
import java.util.HashMap;

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

import io.flowing.retail.kafka.order.adapter.payload.GoodsFetchedEventPayload;
import io.flowing.retail.kafka.order.adapter.payload.GoodsShippedEventPayload;
import io.flowing.retail.kafka.order.adapter.payload.PaymentReceivedEventPayload;
import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.flowing.retail.kafka.order.port.persistence.OrderRepository;
import io.zeebe.gateway.ZeebeClient;
import io.zeebe.gateway.api.events.MessageEvent;

@Component
@EnableBinding(Sink.class)
public class MessageListener {

	@Autowired
	private OrderRepository repository;

	@Autowired
	private ZeebeClient zeebe;

  @StreamListener(target = Sink.INPUT, condition = "(headers['messageType']?:'')=='OrderPlacedEvent'")
  @Transactional
  public void orderPlacedReceived(String messageJson) throws JsonParseException, JsonMappingException, IOException {
    // read data
    Message<Order> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<Order>>() {});
    Order order = message.getPayload();
    
    // persist domain entity
    repository.save(order);

    // prepare data for workflow
    OrderFlowContext context = new OrderFlowContext();
    context.setOrderId(order.getId());
    context.setTraceId(message.getTraceId());

    // and kick of a new flow instance
    System.out.println("New order placed, start flow. " + context.asJson());
    zeebe.topicClient().workflowClient().newCreateInstanceCommand() //
        .bpmnProcessId("order-kafka") //
        .latestVersion() // 
        .payload(context.asJson()) //
        .send().join();
  }

  @StreamListener(target = Sink.INPUT, condition = "(headers['messageType']?:'')=='PaymentReceivedEvent'")
  @Transactional
  public void paymentReceived(String messageJson) throws Exception {
    Message<PaymentReceivedEventPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<PaymentReceivedEventPayload>>() {});

    PaymentReceivedEventPayload event = message.getPayload(); // TODO: Read something from it? 

    MessageEvent messageEvent = zeebe.topicClient().workflowClient().newPublishMessageCommand() //
      .messageName(message.getMessageType())
      .correlationKey(message.getCorrelationId())
      .payload("{\"paymentInfo\": \"YeahWeCouldAddSomething\"}")
      .send().join();
    
    System.out.println("Correlated " + messageEvent );
  }

  @StreamListener(target = Sink.INPUT, condition = "(headers['messageType']?:'')=='GoodsFetchedEvent'")
  @Transactional
  public void goodsFetchedReceived(String messageJson) throws Exception {
    Message<GoodsFetchedEventPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<GoodsFetchedEventPayload>>() {});

    String pickId = message.getPayload().getPickId();     

    MessageEvent messageEvent = zeebe.topicClient().workflowClient().newPublishMessageCommand() //
        .messageName(message.getMessageType())
        .correlationKey(message.getCorrelationId())
        .payload("{\"pickId\":\"" + pickId + "\"}") //
        .send().join();

    System.out.println("Correlated " + messageEvent );
  }


  @StreamListener(target = Sink.INPUT, condition = "(headers['messageType']?:'')=='GoodsShippedEvent'")
  @Transactional
  public void goodsShippedReceived(String messageJson) throws Exception {
    Message<GoodsShippedEventPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<GoodsShippedEventPayload>>() {});

    String shipmentId = message.getPayload().getShipmentId();     

    MessageEvent messageEvent = zeebe.topicClient().workflowClient().newPublishMessageCommand() //
        .messageName(message.getMessageType())
        .correlationKey(message.getCorrelationId())
        .payload("{\"shipmentId\":\"" + shipmentId + "\"}") //
        .send().join();

    System.out.println("Correlated " + messageEvent );
  }
}
