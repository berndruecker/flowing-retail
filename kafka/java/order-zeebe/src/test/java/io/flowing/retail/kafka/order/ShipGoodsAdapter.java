package io.flowing.retail.kafka.order;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.subscription.JobHandler;

@Component
public class ShipGoodsAdapter implements JobHandler {
  
	private ZeebeClient zeebe;

	public void subscribe(ZeebeClient zeebe) {
		this.zeebe = zeebe;
     zeebe.jobClient().newWorker()
      .jobType("ship-goods")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, ActivatedJob event) {
    OrderFlowContext context = OrderFlowContext.fromJson(event.getPayload());
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();

    zeebe.workflowClient().newPublishMessageCommand() //
            .messageName("GoodsShippedEvent")
            .correlationKey(correlationId)
            .payload("{\"shipmentId\":\"635\"}") //
            .send().join();

        System.out.println("Correlated GoodsShippedEvent" );   
        
    client.newCompleteCommand(event.getKey()) //
        .payload(Collections.singletonMap("CorrelationId_ShipGoods", correlationId)) //
        .send().join();
  }  

}
