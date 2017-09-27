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
  }
  
  @RequestMapping(path = "/charge", method = POST)
  public CreateChargeResponse chargeCreditCard(@RequestBody CreateChargeRequest request) {
    CreateChargeResponse response = new CreateChargeResponse();
    
    System.out.println("CHARGE " + request.amount + " ON CREDIT CARD");
    
    response.transactionId = UUID.randomUUID().toString();
    return response;
  }
  

}