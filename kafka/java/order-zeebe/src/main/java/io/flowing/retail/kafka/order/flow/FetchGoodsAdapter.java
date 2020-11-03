package io.flowing.retail.kafka.order.flow;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.flow.payload.FetchGoodsCommandPayload;
import io.flowing.retail.kafka.order.messages.Message;
import io.flowing.retail.kafka.order.messages.MessageSender;
import io.flowing.retail.kafka.order.persistence.OrderRepository;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;

@Component
public class FetchGoodsAdapter {
  
  @Autowired
  private MessageSender messageSender; 
  
  @Autowired
  private OrderRepository orderRepository;

  @ZeebeWorker(type = "fetch-goods")
  public void handle(JobClient client, ActivatedJob job) {
    OrderFlowContext context = OrderFlowContext.fromMap(job.getVariablesAsMap());
    Order order = orderRepository.findById( context.getOrderId() ).get();
    
    // generate an UUID for this communication
    String correlationId = UUID.randomUUID().toString();
        
    messageSender.send(new Message<FetchGoodsCommandPayload>( //
            "FetchGoodsCommand", //
            context.getTraceId(), //
            new FetchGoodsCommandPayload() //
              .setRefId(order.getId()) //
              .setItems(order.getItems())) //
        .setCorrelationid(correlationId));
    
    client.newCompleteCommand(job.getKey()) //
      .variables(Collections.singletonMap("CorrelationId_FetchGoods", correlationId)) //
      .send().join();
  }
  
}
