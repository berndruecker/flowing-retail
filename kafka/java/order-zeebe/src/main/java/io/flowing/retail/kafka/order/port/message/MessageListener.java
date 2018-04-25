package io.flowing.retail.kafka.order.port.message;

import static io.flowing.retail.kafka.order.adapter.ZeebeWorkarounds.createCompleteTaskCommandByCorrelationId;

import java.io.IOException;

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
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.event.TaskEvent;

@Component
@EnableBinding(Sink.class)
public class MessageListener {

	@Autowired
	private OrderRepository repository;

	@Autowired
	private ZeebeClient zeebe;

  @StreamListener(target = Sink.INPUT, condition = "payload.messageType.toString()=='OrderPlacedEvent'")
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
    zeebe.workflows().create("default-topic") //
        .bpmnProcessId("order-kafka") //
        .payload(context.asJson()) //
        .execute();
  }

  @StreamListener(target = Sink.INPUT, condition = "payload.messageType.toString()=='PaymentReceivedEvent'")
  @Transactional
  public void paymentReceived(String messageJson) throws Exception {
    Message<PaymentReceivedEventPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<PaymentReceivedEventPayload>>() {});

    PaymentReceivedEventPayload event = message.getPayload(); // TODO: Read something from it? 

    TaskEvent taskEvent = createCompleteTaskCommandByCorrelationId(zeebe, message.getCorrelationId()).execute();
    System.out.println("Completed " + taskEvent );
  }

  @StreamListener(target = Sink.INPUT, condition = "payload.messageType.toString()=='GoodsFetchedEvent'")
  @Transactional
  public void goodsFetchedReceived(String messageJson) throws Exception {
    Message<GoodsFetchedEventPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<GoodsFetchedEventPayload>>() {});

    String pickId = message.getPayload().getPickId();     

    TaskEvent taskEvent = createCompleteTaskCommandByCorrelationId(zeebe, message.getCorrelationId()) //
        .payload("{\"pickId\":\"" + pickId + "\"}") //
      .execute();
    System.out.println("Completed " + taskEvent );
  }


  @StreamListener(target = Sink.INPUT, condition = "payload.messageType.toString()=='GoodsShippedEvent'")
  @Transactional
  public void goodsShippedReceived(String messageJson) throws Exception {
    Message<GoodsShippedEventPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<GoodsShippedEventPayload>>() {});

    String shipmentId = message.getPayload().getShipmentId();     

    TaskEvent taskEvent = createCompleteTaskCommandByCorrelationId(zeebe, message.getCorrelationId()) //
      .payload("{\"shipmentId\":\"" + shipmentId + "\"}") //
      .execute();
    System.out.println("Completed " + taskEvent );
  }
}
