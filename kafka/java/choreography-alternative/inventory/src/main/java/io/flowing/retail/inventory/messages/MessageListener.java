package io.flowing.retail.inventory.messages;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.flowing.retail.inventory.application.InventoryService;
import io.flowing.retail.inventory.domain.Item;

@Component
@EnableBinding(Sink.class)
public class MessageListener {    
  
  @Autowired
  private MessageSender messageSender;
  
  @Autowired
  private InventoryService inventoryService;

  @StreamListener(target = Sink.INPUT, 
      condition="(headers['messageType']?:'')=='PaymentReceivedEvent'")
  @Transactional
  public void paymentReceived(String messageJson) throws JsonParseException, JsonMappingException, IOException {
    Message<JsonNode> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<JsonNode>>(){});
    
    ObjectNode payload = (ObjectNode) message.getPayload();
    Item[] items = new ObjectMapper().treeToValue(payload.get("items"), Item[].class);
    
    String pickId = inventoryService.pickItems( // 
        Arrays.asList(items), "order", payload.get("orderId").asText());
    
    // as in payment - we have to keep the whole order in the payload 
    // as the data flows through this service
    
    payload.put("pickId", pickId);
    
    messageSender.send( //
        new Message<JsonNode>( //
            "GoodsFetchedEvent", //
            message.getTraceId(), //
            payload));
  }
    
    
}
