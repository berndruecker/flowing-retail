package io.flowing.retail.payment.rest;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

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

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;
import io.zeebe.model.bpmn.Bpmn;
import io.zeebe.model.bpmn.BpmnModelInstance;

/**
 * Step$: Use Zeebe state machine for long-running retry;
 * but try to get a synchronous response
 */
@RestController
public class PaymentRestHacksControllerV4 {

  @Autowired
  private ZeebeClient zeebe;
  
  @Autowired
  private ChargeCreditCardHandler chargeCreditCardHandlerV4;

  private List<JobWorker> workers = new ArrayList<JobWorker>();

  @PostConstruct
  public void createFlowDefinition() {
    BpmnModelInstance flow = Bpmn.createExecutableProcess("paymentV4") //
        .startEvent() //
        .serviceTask("stripe").zeebeTaskType("charge-creditcard-v4") //
          .zeebeTaskRetries(2) //        
        .serviceTask("response").zeebeTaskType("payment-response-v4") //
          // TODO: Still missing in current Zeebe: Ability to use expression language here to start dedicated "Response Queue" for Client
        .endEvent().done();
    
    workers.add( zeebe.newWorker()
      .jobType("charge-creditcard-v4") // 
      .handler(chargeCreditCardHandlerV4) // 
      .open());
    workers.add( zeebe.newWorker()
      .jobType("payment-response-v4") // 
      .handler(new NotifySemaphorHandler()) // 
      .open());
  
    zeebe.newDeployCommand() // 
      .addWorkflowModel(flow, "payment.bpmn") //
      .send().join();
  }
  
  @Component("chargeCreditCardHandlerV4")
  public static class ChargeCreditCardHandler implements JobHandler {

    @Autowired
    private RestTemplate rest;
    private String stripeChargeUrl = "http://localhost:8099/charge";

    @Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
      CreateChargeRequest request = new CreateChargeRequest();
      request.amount = (int) job.getVariablesAsMap().get("amount");

      CreateChargeResponse response = new HystrixCommand<CreateChargeResponse>(HystrixCommandGroupKey.Factory.asKey("stripe")) {
        protected CreateChargeResponse run() throws Exception {
            return rest.postForObject( //
              stripeChargeUrl, //
              request, //
              CreateChargeResponse.class);
        }
      }.execute();
      
      client.newCompleteCommand(job.getKey()) //
        .variables(Collections.singletonMap("paymentTransactionId", response.transactionId))
        .send().join();
    }

  }

  @RequestMapping(path = "/api/payment/v4", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    Semaphore newSemaphore = NotifySemaphorHandler.newSemaphore(traceId);
    chargeCreditCard(traceId, customerId, amount);
    boolean finished = newSemaphore.tryAcquire(500, TimeUnit.MILLISECONDS);
    NotifySemaphorHandler.removeSemaphore(traceId);

    if (finished) {
      return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\"}";
    } else {
      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
    }
  }

  public void chargeCreditCard(String traceId, String customerId, long remainingAmount) {
    HashMap<String, Object> variables = new HashMap<>();
    variables.put("amount", remainingAmount);
    variables.put("traceId", traceId);
    
    zeebe.newCreateInstanceCommand() //
      .bpmnProcessId("paymentV4")
      .latestVersion()
      .variables(variables)
      .send().join();
  }
  
  public static class NotifySemaphorHandler implements JobHandler {
    
    public static Map<String, Semaphore> semaphors = new HashMap<>();

    @Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
      String traceId = (String) job.getVariablesAsMap().get("traceId");
      Semaphore s = semaphors.get(traceId);
      if (s!=null) {
        s.release();
        semaphors.remove(traceId);
      }
      client.newCompleteCommand(job.getKey()).send().join();
    }

    public static Semaphore newSemaphore(String traceId) {
      Semaphore sema = new Semaphore(0);
      semaphors.put(traceId, sema);
      return sema;
    }

    public static void removeSemaphore(String traceId) {
      semaphors.remove(traceId);
    }

  }

  
  public static class CreateChargeRequest {
    public int amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

  @PreDestroy
  public void closeSubscription() {
    workers.forEach((worker) -> worker.close());
  }

}