package io.flowing.retail.kafka.order.adapter;

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
import io.zeebe.gateway.api.subscription.JobHandler;
import io.zeebe.gateway.api.subscription.JobWorker;

@Component
public class FetchGoodsAdapter implements JobHandler {
  
  @Autowired
  private MessageSender messageSender; 
  
  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private ZeebeClient zeebe;

  private JobWorker subscription;
  
  @PostConstruct
  public void subscribe() {
    subscription = zeebe.jobClient().newWorker()
      .jobType("fetch-goods")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @PreDestroy
  public void closeSubscription() {
    subscription.close();      
  }

  @Override
  public void handle(JobClient client, JobEvent event) {
    OrderFlowContext context = OrderFlowContext.fromJson(event.getPayload());
    Order order = orderRepository.findById( context.getOrderId() ).get();
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();
        
    messageSender.send(new Message<FetchGoodsCommandPayload>( //
            "FetchGoodsCommand", //
            context.getTraceId(), //
            new FetchGoodsCommandPayload() //
              .setRefId(order.getId()) //
              .setItems(order.getItems())) //
        .setCorrelationId(correlationId));
    
    client.newCompleteCommand(event) //
      .payload(Collections.singletonMap("CorrelationId_FetchGoods", correlationId)) //
      .send().join();
  }
  
}
