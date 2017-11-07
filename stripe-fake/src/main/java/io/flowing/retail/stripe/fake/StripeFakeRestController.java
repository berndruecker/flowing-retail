package io.flowing.retail.stripe.fake;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.UUID;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StripeFakeRestController {    

  public static class CreateChargeRequest {
    public int amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
    public String errorCode;
  }
  
  @RequestMapping(path = "/charge", method = POST)
  public CreateChargeResponse chargeCreditCard(@RequestBody CreateChargeRequest request) throws Exception {
    CreateChargeResponse response = new CreateChargeResponse();
    
    long waitTimeMillis = 0;
//    long waitTimeMillis = Math.round( Math.random() * 60 * 1000 ); // up to 60 seconds  

    System.out.println("CHARGE " + request.amount + " ON CREDIT CARD ... (takes " + waitTimeMillis/1000 + " seconds)");
    
    Thread.sleep(waitTimeMillis);
    
    if (Math.random() > 0.8d) {
      response.errorCode = "credit card expired";
    }
    
    response.transactionId = UUID.randomUUID().toString();
    return response;
  }
  

}