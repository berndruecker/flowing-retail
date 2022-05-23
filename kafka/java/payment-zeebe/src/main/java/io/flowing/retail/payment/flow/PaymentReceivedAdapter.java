package io.flowing.retail.payment.flow;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.flowing.retail.payment.messages.Message;
import io.flowing.retail.payment.messages.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaymentReceivedAdapter {

  @Autowired
  private MessageSender messageSender;

  @ZeebeWorker(type = "paymentReceived")
  public void paymentReceived(ActivatedJob job) throws Exception {
    String refId = (String) job.getVariablesAsMap().get("refId");
    String correlationId = (String) job.getVariablesAsMap().get("correlationId");
    String traceId = (String) job.getVariablesAsMap().get("traceId");

    messageSender.send( //
        new Message<PaymentReceivedEventPayload>( //
            "PaymentReceivedEvent", //
            traceId, //
            new PaymentReceivedEventPayload() //
                .setRefId(refId))
    		.setCorrelationid(correlationId));
  }

}
