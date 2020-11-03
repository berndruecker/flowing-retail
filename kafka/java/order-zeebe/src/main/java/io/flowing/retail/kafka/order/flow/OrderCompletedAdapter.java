package io.flowing.retail.kafka.order.flow;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.flow.payload.OrderCompletedEventPayload;
import io.flowing.retail.kafka.order.messages.Message;
import io.flowing.retail.kafka.order.messages.MessageSender;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;

@Component
public class OrderCompletedAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @ZeebeWorker(type = "order-completed")
  public void handle(JobClient client, ActivatedJob job) {
    OrderFlowContext context = OrderFlowContext.fromMap(job.getVariablesAsMap());
       
    messageSender.send( //
        new Message<OrderCompletedEventPayload>( //
            "OrderCompletedEvent", //
            context.getTraceId(), //
            new OrderCompletedEventPayload() //
              .setOrderId(context.getOrderId())));
    
    //TODO: Reintorduce traceId?     .setCorrelationId(event.get)));
    
    client.newCompleteCommand(job.getKey()).send().join();
  }

  

}
