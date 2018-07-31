package io.flowing.retail.kafka.order.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.kafka.order.adapter.payload.FetchGoodsCommandPayload;
import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.flowing.retail.kafka.order.port.message.Message;
import io.flowing.retail.kafka.order.port.message.MessageSender;
import io.flowing.retail.kafka.order.port.persistence.OrderRepository;
import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;

@Component
public class FetchGoodsAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @ZeebeTaskListener(taskType = "fetch-goods", lockTime=5*60*1000)
  public void sendFetchGoodsCommand(TasksClient client, TaskEvent taskEvent) throws Exception {
    OrderFlowContext context = OrderFlowContext.fromJson(taskEvent.getPayload());

    Order order = orderRepository.findById( context.getOrderId() ).get(); 
        
    messageSender.send(new Message<FetchGoodsCommandPayload>( //
            "FetchGoodsCommand", //
            context.getTraceId(), //
            new FetchGoodsCommandPayload() //
              .setRefId(order.getId()) //
              .setItems(order.getItems())) //
        .setCorrelationId(ZeebeWorkarounds.getCorrelationId(taskEvent)));
  }
  
}
