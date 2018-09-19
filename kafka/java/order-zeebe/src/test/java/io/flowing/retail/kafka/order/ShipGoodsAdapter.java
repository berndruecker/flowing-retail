package io.flowing.retail.kafka.order;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.adapter.payload.ShipGoodsCommandPayload;
import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.flowing.retail.kafka.order.messages.Message;
import io.flowing.retail.kafka.order.messages.MessageSender;
import io.flowing.retail.kafka.order.persistence.OrderRepository;
import io.zeebe.gateway.ZeebeClient;
import io.zeebe.gateway.api.clients.JobClient;
import io.zeebe.gateway.api.events.JobEvent;
import io.zeebe.gateway.api.events.MessageEvent;
import io.zeebe.gateway.api.subscription.JobHandler;
import io.zeebe.gateway.api.subscription.JobWorker;

@Component
public class ShipGoodsAdapter implements JobHandler {
  
	private ZeebeClient zeebe;

	public void subscribe(ZeebeClient zeebe) {
		this.zeebe = zeebe;
     zeebe.topicClient().jobClient().newWorker()
      .jobType("ship-goods")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, JobEvent event) {
    OrderFlowContext context = OrderFlowContext.fromJson(event.getPayload());
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();

    MessageEvent messageEvent = zeebe.topicClient().workflowClient().newPublishMessageCommand() //
            .messageName("ShippedGoodsEvent")
            .correlationKey(correlationId)
            .payload("{\"shipmentId\":\"635\"}") //
            .send().join();

        System.out.println("Correlated " + messageEvent );   
        
    client.newCompleteCommand(event) //
        .payload(Collections.singletonMap("CorrelationId_ShipGoods", correlationId)) //
        .send().join();
  }  

}
