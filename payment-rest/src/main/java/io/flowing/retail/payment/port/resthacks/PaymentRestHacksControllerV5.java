package io.flowing.retail.payment.port.resthacks;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.flowing.retail.payment.port.rest.NotifySemaphorAdapter;

/**
 * Step5: Use Camunda state machine for long-running retry, external task & compensation
 */
@RestController
public class PaymentRestHacksControllerV5 {

  @Autowired
  private ProcessEngine camunda;

  @RequestMapping(path = "/api/payment/v5", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    long remainingAmount = useExistingCustomerCredit(customerId, amount);

    if (remainingAmount > 0) {

      Semaphore newSemaphore = NotifySemaphorAdapter.newSemaphore(traceId);
      
      ProcessInstance pi = chargeCreditCard(traceId, customerId, remainingAmount);
      
      boolean finished = newSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
      NotifySemaphorAdapter.removeSemaphore(traceId);
      
      if (finished) {
        HistoricVariableInstance historicVariableInstance = camunda.getHistoryService().createHistoricVariableInstanceQuery()
            .processInstanceId(pi.getId()) //
            .variableName("paymentTransactionId") //
            .singleResult();
          if (historicVariableInstance!=null) {
            String paymentTransactionId = (String)historicVariableInstance.getValue(); 
            return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"paymentTransactionId\": \""+paymentTransactionId+"\"}";
          } else {
            return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"payedByCredit\": \"true\"}";        
          }
      } else {
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
      }
    } else {
      return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"payedByCredit\": \"true\"}";
    }
  }

  public ProcessInstance chargeCreditCard(String traceId, String customerId, long remainingAmount) {
    return camunda.getRuntimeService() //
        .startProcessInstanceByKey("payment5", traceId,//
            Variables.putValue("amount", remainingAmount));
  }

  public static class CreateChargeRequest {
    public long amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

  public long useExistingCustomerCredit(String customerId, long amount) {
    long remainingAmount = 0;
    if (Math.random() > 0.5d) {
      remainingAmount = 15;
    }
    return remainingAmount;
  }
}