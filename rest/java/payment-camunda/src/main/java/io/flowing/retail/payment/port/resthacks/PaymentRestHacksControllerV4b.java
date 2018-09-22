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
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import io.flowing.retail.payment.port.resthacks.PaymentRestHacksControllerV3.CreateChargeResponse;
import io.flowing.retail.payment.port.resthacks.adapter.FailingOnLastRetry;
import io.flowing.retail.payment.port.resthacks.adapter.NotifySemaphorAdapter;

/**
 * Step4: Use Camunda state machine for long-running retry + best effort response
 */
@RestController
public class PaymentRestHacksControllerV4b {

  @Autowired
  private ProcessEngine camunda;

  @PostConstruct
  public void createFlowDefinition() {
    BpmnModelInstance flow = Bpmn.createExecutableProcess("paymentV4b") //
        .startEvent() //
        .serviceTask("stripe").camundaDelegateExpression("#{stripeAdapter4b}") //
          .camundaAsyncBefore().camundaFailedJobRetryTimeCycle("R3/PT10S") // 
          .boundaryEvent("noRetries").error("Error_NoRetries") //
            .serviceTask("stripeCancel").camundaExpression("#{stripeCancelAdapter4b}") //
            .camundaAsyncBefore().camundaFailedJobRetryTimeCycle("R3/PT10S") //
            .endEvent()
        .moveToActivity("stripe")
        .endEvent().camundaExecutionListenerClass("start", NotifySemaphorAdapter.class).done();

    camunda.getRepositoryService().createDeployment() //
        .addModelInstance("payment.bpmn", flow) //
        .deploy();
  }

  @Component("stripeAdapter4b")
  public static class StripeAdapter implements JavaDelegate {

    @Autowired
    private RestTemplate rest;
    private String stripeChargeUrl = "http://localhost:8099/charge";

    @FailingOnLastRetry    
    public void execute(DelegateExecution ctx) throws Exception {
      CreateChargeRequest request = new CreateChargeRequest();
      request.amount = (long) ctx.getVariable("amount");

      CreateChargeResponse response = new HystrixCommand<CreateChargeResponse>(HystrixCommandGroupKey.Factory.asKey("stripe")) {
        protected CreateChargeResponse run() throws Exception {
          return rest.postForObject( //
              stripeChargeUrl, //
              request, //
              CreateChargeResponse.class);
        }
      }.execute();
      
      ctx.setVariable("paymentTransactionId", response.transactionId);
    }
  }
  
  @Component("stripeCancelAdapter4b")
  public static class StripeCancelAdapter implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      System.out.println("Making sure the payment is canceled!");
    }
  }  

  @RequestMapping(path = "/api/payment/v4b", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    Semaphore newSemaphore = NotifySemaphorAdapter.newSemaphore(traceId);
    chargeCreditCard(traceId, customerId, amount);
    boolean finished = newSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
    NotifySemaphorAdapter.removeSemaphore(traceId);

    if (finished) {
      return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\"}";
    } else {
      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
    }
  }

  public ProcessInstance chargeCreditCard(String traceId, String customerId, long remainingAmount) {
    return camunda.getRuntimeService() //
        .startProcessInstanceByKey("paymentV4b", traceId,//
            Variables.putValue("amount", remainingAmount));
  }

  public static class CreateChargeRequest {
    public long amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

}