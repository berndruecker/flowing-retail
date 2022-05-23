package io.flowing.retail.payment.flow;

import io.camunda.zeebe.spring.client.annotation.ZeebeVariable;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
@ConfigurationProperties
public class ChargeCreditCardAdapter {

  @Autowired
  private RestTemplate rest;

  /**
   * Of course you could use Eureka for this
   */
  private String stripeChargeUrl = "http://localhost:8099/charge";

  @ZeebeWorker(type = "chargeCreditCard", autoComplete = true)
  public Map<String, String> chargeCreditCard(@ZeebeVariable Integer remaimingAmount) throws Exception {
    CreateChargeRequest request = new CreateChargeRequest();
    request.amount = remaimingAmount;

    CreateChargeResponse response = rest.postForObject( //
        stripeChargeUrl, //
        request, //
        CreateChargeResponse.class);   
    
    // TODO Add error scenarios to StripeFake and then raise "Error_CreditCardError" here
    if (response.errorCode!=null) {
      throw new ZeebeBpmnError("Error_PaymentError", response.errorCode);
    }

    return Collections.singletonMap("paymentTransactionId", response.transactionId);
  }
  
  public static class CreateChargeRequest {
    public int amount;
  }

  public static class CreateChargeResponse {
    public String transactionId;
    public String errorCode;
  }

  public RestTemplate getRest() {
    return rest;
  }

  public void setRest(RestTemplate rest) {
    this.rest = rest;
  }

  public String getStripeChargeUrl() {
    return stripeChargeUrl;
  }

  public void setStripeChargeUrl(String stripeChargeUrl) {
    this.stripeChargeUrl = stripeChargeUrl;
  }

}
