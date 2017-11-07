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
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import io.flowing.retail.payment.port.rest.NotifySemaphorAdapter;
import io.flowing.retail.payment.port.rest.ChargeCreditCardAdapter.CreateChargeRequest;
import io.flowing.retail.payment.port.rest.ChargeCreditCardAdapter.CreateChargeResponse;

/**
 * Step3: Use Camunda state machine for long-running retry
 */
@RestController
public class PaymentRestHacksControllerV3 {

  @Autowired
  private ProcessEngine camunda;

  @PostConstruct
  public void createFlowDefinition() {
    EndEventBuilder flow = Bpmn.createExecutableProcess("payment") //
        .startEvent() //
        .serviceTask().camundaDelegateExpression("#{stripeAdapter}") //
          .camundaAsyncBefore().camundaFailedJobRetryTimeCycle("R3/PT1M") //        
        .scriptTask().scriptFormat("javascript").scriptText("java.lang.System.out.println('[x] done');") //
        .endEvent();
    
    camunda.getRepositoryService().createDeployment() //
      .addModelInstance("payment.bpmn", flow.done()) //
      .deploy();
  }
  
  @Component("stripeAdapter")
  public static class StripeAdapter implements JavaDelegate {

    @Autowired
    private RestTemplate rest;
    private String stripeChargeUrl = "http://localhost:8099/charge";

    public void execute(DelegateExecution ctx) throws Exception {
      CreateChargeRequest request = new CreateChargeRequest();
      request.amount = (long) ctx.getVariable("amount");

      CreateChargeResponse response = rest.postForObject( //
          stripeChargeUrl, //
          request, //
          CreateChargeResponse.class);

      ctx.setVariable("paymentTransactionId", response.transactionId);
    }
  }

  @RequestMapping(path = "/api/payment/v3", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
      String traceId = UUID.randomUUID().toString();
      String customerId = "0815"; // get somehow from retrievePaymentPayload
      long amount = 15; // get somehow from retrievePaymentPayload 
  
      long remainingAmount = 
           useExistingCustomerCredit(customerId, amount);
       
    if (remainingAmount > 0) {       
       chargeCreditCard(customerId, remainingAmount);
       return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
    } else {      
       return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"payedByCredit\": \"true\"}";        
    }
  }

  public void chargeCreditCard(String customerId, long remainingAmount) {
    ProcessInstance pi = camunda.getRuntimeService() //
        .startProcessInstanceByKey("payment", //
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