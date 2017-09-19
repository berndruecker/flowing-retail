package io.flowing.retail.shipping.port.message;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@EnableBinding(Sink.class)
public class MessageListener {    
  
  @Autowired
  private MessageSender messageSender;

  @StreamListener(target = Sink.INPUT, 
      condition="payload.messageType.toString()=='ShipGoodsCommand'")
  @Transactional
  public void shipGoodsCommandReceived(String messageJson) throws Exception {
    Message<ShipGoodsCommandPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<ShipGoodsCommandPayload>>(){});

    // and directly send response, just make up some shipment id
    String shipmentId = UUID.randomUUID().toString();
    
    System.out.println("Shipping to " + message.getPayload().getRecipientAddress());
    
    messageSender.send( //
        new Message<GoodsShippedEventPayload>( //
            "GoodsShippedEvent", //
            message.getTraceId(), //
            new GoodsShippedEventPayload() //
              .setShipmentId(shipmentId)));
  }
    
    
}
