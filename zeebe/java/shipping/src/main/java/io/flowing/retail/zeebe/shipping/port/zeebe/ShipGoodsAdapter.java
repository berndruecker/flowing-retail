package io.flowing.retail.zeebe.shipping.port.zeebe;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;


@Component
public class ShipGoodsAdapter {
  
  @ZeebeTaskListener(taskType = "ship-goods-z", lockTime=5*60*1000)
  public void retrievePayment(TasksClient client, TaskEvent taskEvent) throws Exception {
//    PaymentInput context = new ObjectMapper().readValue(taskEvent.getPayload(), PaymentInput.class);
//    String traceId = context.getTraceId();    
//    
//    String refId = context.getRefId();
//    long amount = context.getAmount();
    
    System.out.println("ship goods");

    client.complete(taskEvent).execute();
  }

}
