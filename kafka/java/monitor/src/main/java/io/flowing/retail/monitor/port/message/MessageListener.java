package io.flowing.retail.monitor.port.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.monitor.domain.PastEvent;
import io.flowing.retail.monitor.port.persistence.LogRepository;

@Component
@EnableBinding(Sink.class)
public class MessageListener {

  @Autowired
  private SimpMessagingTemplate simpMessageTemplate;

  @StreamListener(target = Sink.INPUT)
  @Transactional
  public void messageReceived(String messageJson) throws Exception {
    Message<JsonNode> message = new ObjectMapper().readValue( //
        messageJson, //
        new TypeReference<Message<JsonNode>>() {});
    
    String type = "Event";
    if (message.getMessageType().endsWith("Command")) {
      type = "Command";
    }
    
    PastEvent event = new PastEvent( //
        type, //
        message.getMessageType(), //
        message.getTraceId(), //
        message.getSender(), //
        message.getPayload().toString());
    
    // save
    LogRepository.instance.addEvent(event);
    
    // and probably send to connected websocket (TODO: Not a good place for the code here!)
    simpMessageTemplate.convertAndSend("/topic/events", event);
  }

}
