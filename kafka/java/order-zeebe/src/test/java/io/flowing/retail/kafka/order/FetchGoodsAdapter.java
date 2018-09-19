package io.flowing.retail.kafka.order;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.adapter.payload.FetchGoodsCommandPayload;
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
public class FetchGoodsAdapter implements JobHandler {
  
	public static String correlationId;
	private ZeebeClient zeebe;

	public void subscribe(ZeebeClient zeebe) {
		this.zeebe = zeebe;
    zeebe.topicClient().jobClient().newWorker()
      .jobType("fetch-goods")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, JobEvent event) {
    OrderFlowContext context = OrderFlowContext.fromJson(event.getPayload());
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();
        

        
    client.newCompleteCommand(event) //
      .payload(Collections.singletonMap("CorrelationId_FetchGoods", correlationId)) //
      .send().join();
    
    MessageEvent messageEvent = zeebe.topicClient().workflowClient().newPublishMessageCommand() //
            .messageName("GoodsFetchedEvent")
            .correlationKey(correlationId)
            .payload("{\"pickId\":\"99\"}") //
            .send().join();

        System.out.println("Correlated " + messageEvent );
    
  }
  
}
