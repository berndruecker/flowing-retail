package io.flowing.retail.payment.rest;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;



/**
 * Step3: Use Zeebe workflow engine for long-running retry
 */
@RestController
public class PaymentRestHacksControllerV3 {

  @Autowired
  private ZeebeClient zeebe;

  @PostConstruct
  public void createFlowDefinition() {
    BpmnModelInstance flow = Bpmn.createExecutableProcess("paymentV3") //
        .startEvent() //
        .serviceTask("stripe").zeebeJobType("charge-creditcard-v3") //
          .zeebeJobRetries("2") //
        .endEvent().done();
    
    zeebe.newDeployResourceCommand() //
        .addProcessModel(flow, "payment.bpmn") //
        .send().join();
  }
  
  @Component
  public static class ChargeCreditCardHandler  {

    @Autowired
    private RestTemplate rest;
    private String stripeChargeUrl = "http://localhost:8099/charge";

    @JobWorker(type = "charge-creditcard-v3")
    @CircuitBreaker(name = "creditcard")
	public Map<String, String> handleJob(@Variable int amount) throws Exception {
      CreateChargeRequest request = new CreateChargeRequest();
      request.amount = amount;

      CreateChargeResponse response = rest.postForObject( //
              stripeChargeUrl, //
              request, //
              CreateChargeResponse.class);

      return Collections.singletonMap("paymentTransactionId", response.transactionId);
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
    zeebe.newCreateInstanceCommand() //
      .bpmnProcessId("paymentV3")
      .latestVersion()
      .variables(Collections.singletonMap("amount", remainingAmount))
      .send().join();
  }
  
  public static class CreateChargeRequest {
    public int amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

}