package io.flowing.retail.payment.rest;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

/**
 * Step2: Use circuit breaker to avoid cascading failures on slow REST calls
 * and Resilience4j to retry calls
 */
@RestController
public class PaymentRestHacksControllerV2b {
    
  @Autowired
  private RestTemplate rest;
  private String stripeChargeUrl = "http://localhost:8099/charge";
  
  @RequestMapping(path = "/api/payment/v2b", method = PUT)
  @CircuitBreaker(name = "creditcard")
  @Retry(name = "creditcard")
  public String retrievePayment(String retrievePaymentPayload) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    System.out.println("Call to charge credit card...");
    chargeCreditCard(customerId, amount);
    return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\"}";
  }

  public String chargeCreditCard(String customerId, long remainingAmount) {
    CreateChargeRequest request = new CreateChargeRequest();
    request.amount = remainingAmount;

    CreateChargeResponse response = rest.postForObject( //
            stripeChargeUrl, //
            request, //
            CreateChargeResponse.class);

    return response.transactionId;
  }


  private String fallback(String customerId, long remainingAmount, RuntimeException e) {
    throw e;
  }
  public static class CreateChargeRequest {
    public long amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

}