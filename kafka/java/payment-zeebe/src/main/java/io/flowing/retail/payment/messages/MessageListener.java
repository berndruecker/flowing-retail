package io.flowing.retail.payment.messages;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;

@Component
public class MessageListener {  
  
  @Autowired
  private ZeebeClient zeebeClient;

  @Autowired
  private ObjectMapper objectMapper;

  @Transactional
  @KafkaListener(id = "payment", topics = MessageSender.TOPIC_NAME)
  public void messageReceived(String messagePayloadJson, @Header("type") String messageType) throws Exception{
    if ("RetrievePaymentCommand".equals(messageType)) {
      Message<RetrievePaymentCommandPayload> message = objectMapper.readValue(messagePayloadJson, new TypeReference<Message<RetrievePaymentCommandPayload>>(){});
      RetrievePaymentCommandPayload retrievePaymentCommand = message.getData();

      System.out.println("Retrieve payment: " + retrievePaymentCommand.getAmount() + " for " + retrievePaymentCommand.getRefId());

      HashMap<String, Object> variables = new HashMap<>();
      variables.put("amount", retrievePaymentCommand.getAmount());
      variables.put("remainingAmount", retrievePaymentCommand.getAmount());
      variables.put("refId", retrievePaymentCommand.getRefId());
      variables.put("correlationId", message.getCorrelationid());
      variables.put("traceId", message.getTraceid());


      zeebeClient.newPublishMessageCommand().messageName(message.getType()) //
              .correlationKey(message.getTraceid())
              .variables(variables)
              .send().join();
    }
  }
    
}
