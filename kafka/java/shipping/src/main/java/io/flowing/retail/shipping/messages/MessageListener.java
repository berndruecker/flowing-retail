package io.flowing.retail.shipping.messages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.shipping.application.ShippingService;

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
      condition="(headers['type']?:'')=='ShipGoodsCommand'")
  @Transactional
  public void shipGoodsCommandReceived(String messageJson) throws Exception {
    Message<ShipGoodsCommandPayload> message = objectMapper.readValue(messageJson, new TypeReference<Message<ShipGoodsCommandPayload>>(){});

    String shipmentId = shippingService.createShipment( //
        message.getData().getPickId(), //
        message.getData().getRecipientName(), //
        message.getData().getRecipientAddress(), //
        message.getData().getLogisticsProvider());
        
    messageSender.send( //
        new Message<GoodsShippedEventPayload>( //
            "GoodsShippedEvent", //
            message.getTraceid(), //
            new GoodsShippedEventPayload() //
              .setRefId(message.getData().getRefId())
              .setShipmentId(shipmentId))
        .setCorrelationid(message.getCorrelationid()));
  }
    
    
}
