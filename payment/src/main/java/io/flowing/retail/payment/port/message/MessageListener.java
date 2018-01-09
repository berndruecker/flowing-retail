package io.flowing.retail.payment.port.message;

import java.io.IOException;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.payment.port.adapter.PaymentReceivedEventPayload;

@Component
@EnableBinding(Sink.class)
public class MessageListener {  
  
  @Autowired
  private ProcessEngine camunda;
  
  @Autowired
  private MessageSender messageSender;

  @Value("${flowing-retail.verbose}")
  private boolean verbose;

  @StreamListener(target = Sink.INPUT,
      condition="payload.messageType.toString()=='RetrievePaymentCommand'")
  @Transactional
  public void retrievePaymentCommandReceived(String messageJson) throws JsonParseException, JsonMappingException, IOException {
    Message<RetrievePaymentCommandPayload> message = new ObjectMapper().readValue(messageJson, new TypeReference<Message<RetrievePaymentCommandPayload>>(){});
    RetrievePaymentCommandPayload retrievePaymentCommand = message.getPayload();

    if (verbose) {
      System.out.println("Retrieve payment: " + retrievePaymentCommand.getAmount() + " for " + retrievePaymentCommand.getRefId());
    }
    
    camunda.getRuntimeService().createMessageCorrelation(message.getMessageType()) //
      .processInstanceBusinessKey(message.getTraceId())
      .setVariable("amount", retrievePaymentCommand.getAmount()) //
      .setVariable("remainingAmount", retrievePaymentCommand.getAmount()) //
      .setVariable("refId", retrievePaymentCommand.getRefId()) //
      .correlateWithResult();    
  }
    
    
}
