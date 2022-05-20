package io.flowing.retail.shipping.messages;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.flowing.retail.shipping.application.ShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MessageListener {    
  
  @Autowired
  private MessageSender messageSender;
  
  @Autowired
  private ShippingService shippingService;
  
  @Autowired
  private ObjectMapper objectMapper;
  
  @Transactional
  @KafkaListener(id = "shipping", topics = MessageSender.TOPIC_NAME)
  public void messageReceived(String messagePayloadJson, @Header("type") String messageType) throws Exception{
    if ("ShipGoodsCommand".equals(messageType)) {
      Message<ShipGoodsCommandPayload> message = objectMapper.readValue(messagePayloadJson, new TypeReference<Message<ShipGoodsCommandPayload>>() {});

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
    
    
}
