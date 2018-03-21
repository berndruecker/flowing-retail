package io.flowing.retail.kafka.order.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.adapter.payload.OrderCompletedEventPayload;
import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.flowing.retail.kafka.order.port.message.Message;
import io.flowing.retail.kafka.order.port.message.MessageSender;
import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;

@Component
public class OrderCompletedAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @ZeebeTaskListener(taskType = "order-completed", lockTime=5*60*1000)
  public void sendOrderCompletedEvent(TasksClient client, TaskEvent taskEvent) throws Exception {
    OrderFlowContext context = OrderFlowContext.fromJson(taskEvent.getPayload());
       
    messageSender.send( //
        new Message<OrderCompletedEventPayload>( //
            "OrderCompletedEvent", //
            context.getTraceId(), //
            new OrderCompletedEventPayload() //
              .setOrderId(context.getOrderId()))
        .setCorrelationId(ZeebeWorkarounds.getCorrelationId(taskEvent)));
    
    client.complete(taskEvent).execute();
  }

  

}
