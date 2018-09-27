package io.flowing.retail.payment.rest;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

import io.zeebe.gateway.ZeebeClient;
import io.zeebe.gateway.api.clients.JobClient;
import io.zeebe.gateway.api.events.JobEvent;
import io.zeebe.gateway.api.subscription.JobHandler;
import io.zeebe.gateway.api.subscription.JobWorker;
import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.model.bpmn.BpmnModelInstance;

/**
 * Step3: Use Zeebe state machine for long-running retry
 */
@RestController
public class PaymentRestHacksControllerV3 {

  @Autowired
  private ZeebeClient zeebe;
  
  @Autowired
  private ChargeCreditCardHandler handler;

  private JobWorker worker;

  @PostConstruct
  public void createFlowDefinition() {
    BpmnModelInstance flow = Bpmn.createExecutableProcess("paymentV3") //
        .startEvent() //
        .serviceTask("stripe").zeebeTaskType("charge-creditcard-v3") //
          .zeebeTaskRetries(2) //        
        .endEvent().done();
    
    zeebe.workflowClient().newDeployCommand() // 
      .addWorkflowModel(flow, "payment.bpmn") //
      .send().join();

    worker = zeebe.jobClient().newWorker()
        .jobType("charge-creditcard-v3") // 
        .handler(handler) // 
        .open();  
  }
  
  @Component
  public static class ChargeCreditCardHandler implements JobHandler {

    @Autowired
    private RestTemplate rest;
    private String stripeChargeUrl = "http://localhost:8099/charge";

    @Override
    public void handle(JobClient client, JobEvent job) {
      CreateChargeRequest request = new CreateChargeRequest();
      request.amount = (int) job.getPayloadAsMap().get("amount");

      CreateChargeResponse response = new HystrixCommand<CreateChargeResponse>(HystrixCommandGroupKey.Factory.asKey("stripe")) {
        protected CreateChargeResponse run() throws Exception {
            return rest.postForObject( //
              stripeChargeUrl, //
              request, //
              CreateChargeResponse.class);
        }
      }.execute();
      
      client.newCompleteCommand(job) //
        .payload(Collections.singletonMap("paymentTransactionId", response.transactionId))
        .send().join();
    }

  }

  @RequestMapping(path = "/api/payment/v3", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    chargeCreditCard(customerId, amount);
    
    response.setStatus(HttpServletResponse.SC_ACCEPTED);    
    return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
  }

  public void chargeCreditCard(String customerId, long remainingAmount) {
    zeebe.workflowClient().newCreateInstanceCommand() //
      .bpmnProcessId("paymentV3")
      .latestVersion()
      .payload(Collections.singletonMap("amount", remainingAmount))
      .send().join();
  }
  
  public static class CreateChargeRequest {
    public int amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

  @PreDestroy
  public void closeSubscription() {
    worker.close();
  }

}