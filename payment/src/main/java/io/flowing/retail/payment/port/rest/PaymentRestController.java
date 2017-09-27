package io.flowing.retail.payment.port.rest;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentRestController {
  
  @Autowired
  private ProcessEngine camunda;
  
  @RequestMapping(path = "/api/payment/charge", method = PUT)
  public String retrievePayment(String retrievePaymentPayload) throws InterruptedException {
    String traceId = UUID.randomUUID().toString();
    
    ProcessInstance pi = camunda.getRuntimeService().startProcessInstanceByKey(
        "payment-rest", 
        traceId, 
        Variables.putValue("payload", retrievePaymentPayload));
    
    // Now we could wait and poll for the process to finish (or to implement a callback)
    boolean finished = NotifySemaphorAdapter.newSemaphore(traceId).tryAcquire(500, TimeUnit.MILLISECONDS);    
    if (finished) {      
      String paymentTransactionId = (String) camunda.getHistoryService().createHistoricVariableInstanceQuery()
        .processInstanceId(pi.getId()) //
        .variableName("paymentTransactionId") //
        .singleResult().getValue();
      return "{\"traceId\": \"" + traceId + "\", \"paymentTransactionId\": \""+paymentTransactionId+"\"}";
    } else {      
      return "{\"traceId\": \"" + traceId + "\"}";
    }
  }

}