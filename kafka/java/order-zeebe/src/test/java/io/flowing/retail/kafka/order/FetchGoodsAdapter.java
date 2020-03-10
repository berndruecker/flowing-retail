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
public class FetchGoodsAdapter implements JobHandler {
  
	public static String correlationId;
	private ZeebeClient zeebe;

	public void subscribe(ZeebeClient zeebe) {
		this.zeebe = zeebe;
    zeebe.newWorker()
      .jobType("fetch-goods")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, ActivatedJob event) {
    OrderFlowContext context = OrderFlowContext.fromMap(event.getVariablesAsMap());
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();
        

        
    client.newCompleteCommand(event.getKey()) //
      .variables(Collections.singletonMap("CorrelationId_FetchGoods", correlationId)) //
      .send().join();
    
    zeebe.newPublishMessageCommand() //
            .messageName("GoodsFetchedEvent")
            .correlationKey(correlationId)
            .variables("{\"pickId\":\"99\"}") //
            .send().join();

        System.out.println("Correlated GoodsFetchedEvent");
    
  }
  
}
