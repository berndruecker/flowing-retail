package io.flowing.retail.payment.rest;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobHandler;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.value.ZeebeWorkerValue;
import io.camunda.zeebe.spring.client.jobhandling.JobWorkerManager;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Step$: Use Zeebe for long-running retry;
 * but try to get a synchronous response in the happy case
 */
@RestController
public class PaymentRestHacksControllerV4 {

  @Autowired
  private ZeebeClient zeebe;

  @PostConstruct
  public void createFlowDefinition() {
    BpmnModelInstance flow = Bpmn.createExecutableProcess("paymentV4") //
        .startEvent() //
        .serviceTask("stripe").zeebeJobType("charge-creditcard-v4") //
          .zeebeJobRetries("2") //
        .serviceTask("response").zeebeJobType("=\"payment-response-v4-\" + traceId") //
        .endEvent().done();

    // Note: Some ids get auto generated in this case (e.g. sequence flows) which leads to new versions of the process with every deployment
    // Might be beneficial to define process in XML instead of Java, or sort versioning yourself just redeploying when necessary (e.g. using an extension element to mark the logical version)

    // To inspect the generated model: System.out.println(Bpmn.convertToString(flow));

    zeebe.newDeployResourceCommand() //
       .addProcessModel(flow, "payment.bpmn") //
       .send().join();
  }
  
  @Component("chargeCreditCardHandlerV4")
  public static class ChargeCreditCardHandler {

    @Autowired
    private RestTemplate rest;
    private String stripeChargeUrl = "http://localhost:8099/charge";

    @JobWorker(type = "charge-creditcard-v4")
    @CircuitBreaker(name = "creditcard")
	public Map<String, String> handleJob(ActivatedJob job) throws Exception {
      CreateChargeRequest request = new CreateChargeRequest();
      request.amount = (int) job.getVariablesAsMap().get("amount");

      CreateChargeResponse response = rest.postForObject( //
              stripeChargeUrl, //
              request, //
              CreateChargeResponse.class);

      return Collections.singletonMap("paymentTransactionId", response.transactionId);
    }

  }

  @RequestMapping(path = "/api/payment/v4", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    Semaphore newSemaphore = NotifySemaphorHandler.newSemaphore(traceId);
    chargeCreditCard(traceId, customerId, amount);
    boolean finished = newSemaphore.tryAcquire(2, TimeUnit.SECONDS);
    NotifySemaphorHandler.removeSemaphore(traceId);

    if (finished) {
      return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\"}";
    } else {
      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      return "{\"status\":\"pending\", \"traceId\": \"" + traceId + "\"}";
    }
  }

  @Autowired
  private JobWorkerManager jobWorkerManager;

  public void chargeCreditCard(String traceId, String customerId, long remainingAmount) {
    HashMap<String, Object> variables = new HashMap<>();
    variables.put("amount", remainingAmount);
    variables.put("traceId", traceId);
    
    zeebe.newCreateInstanceCommand() //
      .bpmnProcessId("paymentV4")
      .latestVersion()
      .variables(variables)
      .send().join();

    NotifySemaphorHandler notifySemaphorHandler = new NotifySemaphorHandler();
    io.camunda.zeebe.client.api.worker.JobWorker jobWorker = jobWorkerManager.openWorker(
            zeebe,
            new ZeebeWorkerValue().setType("payment-response-v4-" + traceId),
            notifySemaphorHandler);
    notifySemaphorHandler.attachWorker(jobWorkerManager, jobWorker);
    // You can also find an example using Mono and Flux here:
    // https://github.com/camunda-community-hub/camunda-8-examples/blob/main/synchronous-response-springboot/src/main/java/org/example/camunda/process/solution/facade/ProcessController.java
  }
  
  public static class NotifySemaphorHandler implements JobHandler {
    
    public static Map<String, Semaphore> semaphors = new HashMap<>();

    private JobWorkerManager jobWorkerManager;
    private io.camunda.zeebe.client.api.worker.JobWorker jobWorker;

    @Override
	public void handle(JobClient client, ActivatedJob job) throws Exception {
      String traceId = (String) job.getVariablesAsMap().get("traceId");
      Semaphore s = semaphors.get(traceId);
      if (s!=null) {
        s.release();
        semaphors.remove(traceId);
      }
      client.newCompleteCommand(job.getKey()).send();
      jobWorkerManager.closeWorker(jobWorker);
    }

    public void attachWorker(JobWorkerManager jobWorkerManager, io.camunda.zeebe.client.api.worker.JobWorker jobWorker) {
      this.jobWorkerManager = jobWorkerManager;
      this.jobWorker = jobWorker;
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

}