package io.flowing.retail.kafka.order.messages;

import java.io.IOException;
import java.util.Collections;

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

import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.flow.OrderFlowContext;
import io.flowing.retail.kafka.order.flow.payload.GoodsFetchedEventPayload;
import io.flowing.retail.kafka.order.flow.payload.GoodsShippedEventPayload;
import io.flowing.retail.kafka.order.flow.payload.PaymentReceivedEventPayload;
import io.flowing.retail.kafka.order.persistence.OrderRepository;
import io.zeebe.client.ZeebeClient;

@Component
@EnableBinding(Sink.class)
public class MessageListener {

	@Autowired
	private OrderRepository repository;

	@Autowired
	private ZeebeClient zeebe;
	
  @Autowired
  private ObjectMapper objectMapper;

  @StreamListener(target = Sink.INPUT,
          condition="(headers['type']?:'')=='OrderPlacedEvent'")
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

  @StreamListener(target = Sink.INPUT, condition = "(headers['type']?:'')=='PaymentReceivedEvent'")
  @Transactional
  public void paymentReceived(String messageJson) throws Exception {
    Message<PaymentReceivedEventPayload> message = objectMapper.readValue(messageJson, new TypeReference<Message<PaymentReceivedEventPayload>>() {});

    PaymentReceivedEventPayload event = message.getData(); // TODO: Read something from it? 

    zeebe.newPublishMessageCommand() //
      .messageName(message.getType())
      .correlationKey(message.getCorrelationid())
      .variables(Collections.singletonMap("paymentInfo", "YeahWeCouldAddSomething"))
      .send().join();
    
    System.out.println("Correlated " + message );
  }

  @StreamListener(target = Sink.INPUT, condition = "(headers['type']?:'')=='GoodsFetchedEvent'")
  @Transactional
  public void goodsFetchedReceived(String messageJson) throws Exception {
    Message<GoodsFetchedEventPayload> message = objectMapper.readValue(messageJson, new TypeReference<Message<GoodsFetchedEventPayload>>() {});

    String pickId = message.getData().getPickId();     

    zeebe.newPublishMessageCommand() //
        .messageName(message.getType()) //
        .correlationKey(message.getCorrelationid()) // 
        .variables(Collections.singletonMap("pickId", pickId)) //
        .send().join();

    System.out.println("Correlated " + message );
  }


  @StreamListener(target = Sink.INPUT, condition = "(headers['type']?:'')=='GoodsShippedEvent'")
  @Transactional
  public void goodsShippedReceived(String messageJson) throws Exception {
    Message<GoodsShippedEventPayload> message = objectMapper.readValue(messageJson, new TypeReference<Message<GoodsShippedEventPayload>>() {});

    String shipmentId = message.getData().getShipmentId();     

    zeebe.newPublishMessageCommand() //
        .messageName(message.getType()) //
        .correlationKey(message.getCorrelationid()) //
        .variables(Collections.singletonMap("shipmentId", shipmentId)) //
        .send().join();

    System.out.println("Correlated " + message );
  }
}
