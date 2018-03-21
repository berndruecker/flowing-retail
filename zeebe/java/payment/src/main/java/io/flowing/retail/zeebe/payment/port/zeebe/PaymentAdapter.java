package io.flowing.retail.zeebe.payment.port.zeebe;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;


@Component
public class PaymentAdapter {
  
  @ZeebeTaskListener(taskType = "retrieve-payment-z", lockTime=5*60*1000)
  public void retrievePayment(TasksClient client, TaskEvent taskEvent) throws Exception {
    PaymentInput context = new ObjectMapper().readValue(taskEvent.getPayload(), PaymentInput.class);
    String traceId = context.getTraceId();    
    
    String refId = context.getRefId();
    long amount = context.getAmount();
    
    System.out.println("retrieved payment " + amount + " for " + refId);

    client.complete(taskEvent).execute();
  }

}
