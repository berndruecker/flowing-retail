package io.flowing.retail.payment.port.resthacks;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * Step1: Simply call REST Service
 */
@RestController
public class PaymentRestHacksControllerV1 {
  
  @Autowired
  private RestTemplate rest;
  private String stripeChargeUrl = "http://localhost:8099/charge";
  
  @RequestMapping(path = "/api/payment/v1", method = PUT)
  public String retrievePayment(String retrievePaymentPayload, HttpServletResponse response) throws Exception {
    String traceId = UUID.randomUUID().toString();
    String customerId = "0815"; // get somehow from retrievePaymentPayload
    long amount = 15; // get somehow from retrievePaymentPayload 

    long remainingAmount = 
         useExistingCustomerCredit(customerId, amount);
       
    if (remainingAmount > 0) {       
       chargeCreditCard(customerId, remainingAmount);
       return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\"}";
    } else {      
      return "{\"status\":\"completed\", \"traceId\": \"" + traceId + "\", \"payedByCredit\": \"true\"}";        
    }
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