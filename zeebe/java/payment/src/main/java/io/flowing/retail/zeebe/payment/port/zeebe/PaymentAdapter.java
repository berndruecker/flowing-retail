package io.flowing.retail.zeebe.payment.port.zeebe;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;


@Component
public class PaymentAdapter {
  
  @ZeebeTaskListener(taskType = "retrieve-payment-z", lockTime=5*60*1000)
  public void retrievePayment(TasksClient zeebe, TaskEvent taskEvent) throws Exception {
    PaymentInput data = new ObjectMapper().readValue(taskEvent.getPayload(), PaymentInput.class);
    String traceId = data.getTraceId();    
    
    String refId = data.getRefId();
    long amount = data.getAmount();
    
    System.out.println("retrieved payment " + amount + " for " + refId);

    zeebe.complete(taskEvent).execute();
  }

}
