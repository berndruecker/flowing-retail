package io.flowing.retail.payment.messages;

import java.io.IOException;

import org.camunda.bpm.engine.ProcessEngine;
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

import io.flowing.retail.payment.flow.PaymentReceivedEventPayload;

@Component
@EnableBinding(Sink.class)
public class MessageListener {  
  
  @Autowired
  private ProcessEngine camunda;
  
  @Autowired
  private MessageSender messageSender;

  @StreamListener(target = Sink.INPUT, 
      condition="(headers['messageType']?:'')=='RetrievePaymentCommand'")
  @Transactional
  public void retrievePaymentCommandReceived(String messageJson) throws JsonParseException, JsonMappingException, IOException {
    Message<RetrievePaymentCommandPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<RetrievePaymentCommandPayload>>(){});
    RetrievePaymentCommandPayload retrievePaymentCommand = message.getPayload();    
    
    System.out.println("Retrieve payment: " + retrievePaymentCommand.getAmount() + " for " + retrievePaymentCommand.getRefId());
    
    camunda.getRuntimeService().createMessageCorrelation(message.getMessageType()) //
      .processInstanceBusinessKey(message.getTraceId())
      .setVariable("amount", retrievePaymentCommand.getAmount()) //
      .setVariable("remainingAmount", retrievePaymentCommand.getAmount()) //
      .setVariable("refId", retrievePaymentCommand.getRefId()) //
      .setVariable("correlationId", message.getCorrelationId()) //
      .correlateWithResult();    
  }
    
    
}
