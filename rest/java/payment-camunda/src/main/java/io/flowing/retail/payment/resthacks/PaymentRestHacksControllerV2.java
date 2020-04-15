package io.flowing.retail.payment.resthacks;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;

/**
 * Step2: Use Hystrix circuit breaker to make secured REST call
 */
@RestController
public class PaymentRestHacksControllerV2 {
    
  @Autowired
  private RestTemplate rest;
  private String stripeChargeUrl = "http://localhost:8099/charge";
  
  @RequestMapping(path = "/payment/v2", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload

    chargeCreditCard(customerId, amount);    
    return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\"}";
  }

  public String chargeCreditCard(String customerId, long remainingAmount) {
    CreateChargeRequest request = new CreateChargeRequest();
    request.amount = remainingAmount;

    CreateChargeResponse response = new HystrixCommand<CreateChargeResponse>(HystrixCommandGroupKey.Factory.asKey("stripe")) {
      protected CreateChargeResponse run() throws Exception {
        return rest.postForObject( //
            stripeChargeUrl, //
            request, //
            CreateChargeResponse.class);
      }
    }.execute();
    
    
    return response.transactionId;
  }
  
  public static class CreateChargeRequest {
    public long amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
  }

}