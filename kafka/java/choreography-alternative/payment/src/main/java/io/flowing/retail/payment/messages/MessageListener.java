package io.flowing.retail.payment.messages;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.flowing.retail.payment.application.PaymentService;

@Component
@EnableBinding(Sink.class)
public class MessageListener {    
  
  @Autowired
  private MessageSender messageSender;
  
  @Autowired
  private PaymentService paymentService;

  @Autowired
  private ObjectMapper objectMapper;

  @StreamListener(target = Sink.INPUT, 
      condition="(headers['type']?:'')=='OrderPlacedEvent'")
  @Transactional
  public void orderPlaced(String messageJson) throws Exception {
    // Note that we now have to read order data from this message!
    // Bad smell 1 (reading some event instead of dedicated command)
    JsonNode message = objectMapper.readTree(messageJson);
    ObjectNode payload = (ObjectNode) message.get("data");
    
    String orderId = payload.get("orderId").asText();
    if (orderId == null) {
      // We do not yet have an order id - as the responsibility who creates that is unclear 
      // Bad smell 2 (order context missing)
      // But actually not that problematic - as a good practice could be to 
      // generate it in the checkout anyway to improve idempotency
      orderId = UUID.randomUUID().toString();
      payload.put("orderId", orderId);
    }
    // the totalSum needs to be calculated by the checkout in this case - responsibility unclear
    // as this is not done we have to calculate it here - which means we have to learn to much about orders!
    // Bad smell 3 (order context missing)
    long amount = payload.get("items").iterator().next().get("amount").asLong();
    //long amount = payload.get("totalSum").asLong();

    String paymentId = paymentService.createPayment(orderId, amount);    
       
    // Note that we need to pass along the whole order object
    // Maybe with additional data we have
    // Bad smell 4 (data flow passing through - data might grow big and most data is not needed for payment)
    payload.put("paymentId", paymentId);
    
    messageSender.send( //
        new Message<JsonNode>( //
            "PaymentReceivedEvent", //
            message.get("traceid").asText(), //
            message.get("data")));
  }
    
    
}
