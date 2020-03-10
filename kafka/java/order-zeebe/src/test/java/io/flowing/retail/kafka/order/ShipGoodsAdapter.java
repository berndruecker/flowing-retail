package io.flowing.retail.kafka.order;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.flow.OrderFlowContext;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;

@Component
public class ShipGoodsAdapter implements JobHandler {
  
	private ZeebeClient zeebe;

	public void subscribe(ZeebeClient zeebe) {
		this.zeebe = zeebe;
     zeebe.newWorker()
      .jobType("ship-goods")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, ActivatedJob event) {
    OrderFlowContext context = OrderFlowContext.fromMap(event.getVariablesAsMap());
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();

    zeebe.newPublishMessageCommand() //
            .messageName("GoodsShippedEvent")
            .correlationKey(correlationId)
            .variables("{\"shipmentId\":\"635\"}") //
            .send().join();

        System.out.println("Correlated GoodsShippedEvent" );   
        
    client.newCompleteCommand(event.getKey()) //
        .variables(Collections.singletonMap("CorrelationId_ShipGoods", correlationId)) //
        .send().join();
  }  

}
