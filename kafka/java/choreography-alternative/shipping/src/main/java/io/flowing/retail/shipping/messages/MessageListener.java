package io.flowing.retail.shipping.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.flowing.retail.shipping.application.ShippingService;
import io.flowing.retail.shipping.messages.payload.GoodsFetchedEventPayload;

@Component
@EnableBinding(Sink.class)
public class MessageListener {    
  
  @Autowired
  private MessageSender messageSender;
  
  @Autowired
  private ShippingService shippingService;

  @Autowired
  private ObjectMapper objectMapper;

  @StreamListener(target = Sink.INPUT, 
      condition="(headers['type']?:'')=='GoodsFetchedEvent'")
  @Transactional
  public void shipGoodsCommandReceived(String messageJson) throws Exception {
    Message<JsonNode> message = objectMapper.readValue(messageJson, new TypeReference<Message<JsonNode>>(){});
    ObjectNode payload = (ObjectNode) message.getData();
    GoodsFetchedEventPayload payloadEvent = objectMapper //
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) //
        .treeToValue(payload, GoodsFetchedEventPayload.class);

    String shipmentId = shippingService.createShipment( //
        payloadEvent.getPickId(), //
        payloadEvent.getCustomer().getName(), //
        payloadEvent.getCustomer().getAddress(), //
        "DHL");
    
    payload.put("shipmentId", shipmentId);
        
    messageSender.send( //
        new Message<JsonNode>( //
            "GoodsShippedEvent", //
            message.getTraceid(), //
            payload));
    // as nobody else can send an order completed event I will issue it here
    // Bad smell (order context is missing)
    messageSender.send( //
        new Message<JsonNode>( //
            "OrderCompletedEvent", //
            message.getTraceid(), //
            payload));
  }
    
    
}
