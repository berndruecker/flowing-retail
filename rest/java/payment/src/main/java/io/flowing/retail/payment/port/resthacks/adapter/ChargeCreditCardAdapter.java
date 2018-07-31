package io.flowing.retail.payment.port.resthacks.adapter;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConfigurationProperties
public class ChargeCreditCardAdapter implements JavaDelegate {

  @Autowired
  private RestTemplate rest;

  /**
   * Of course you could use Eureka for this
   */
  private String stripeChargeUrl = "http://localhost:8099/charge";

  @FailingOnLastRetry
  public void execute(DelegateExecution ctx) throws Exception {
    CreateChargeRequest request = new CreateChargeRequest();
    request.amount = (int) ctx.getVariable("remainingAmount");

    CreateChargeResponse response = rest.postForObject( //
        stripeChargeUrl, //
        request, //
        CreateChargeResponse.class);
    
    if (response.errorCode != null && response.errorCode.length() > 0) {
      // raise error to be handled in BPMN model in case there was an error in credit card handling
      ctx.setVariable("creditCardErrorCode", response.errorCode);
      throw new BpmnError("Error_CreditCardError");
    }

    ctx.setVariable("paymentTransactionId", response.transactionId);
  }
  
  public static class CreateChargeRequest {
    public long amount;
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
