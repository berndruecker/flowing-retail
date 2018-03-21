package io.flowing.retail.kafka.order.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.adapter.payload.ShipGoodsCommandPayload;
import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.flowing.retail.kafka.order.port.message.Message;
import io.flowing.retail.kafka.order.port.message.MessageSender;
import io.flowing.retail.kafka.order.port.persistence.OrderRepository;
import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;

@Component
public class ShipGoodsAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @ZeebeTaskListener(taskType = "ship-goods", lockTime=5*60*1000)
  public void sendRetrievePaymentCommand(TasksClient client, TaskEvent taskEvent) throws Exception {
    OrderFlowContext context = OrderFlowContext.fromJson(taskEvent.getPayload());

    Order order = orderRepository.getOrder(context.getOrderId()); 

    messageSender.send(new Message<ShipGoodsCommandPayload>( //
            "ShipGoodsCommand", //
            context.getTraceId(), //
            new ShipGoodsCommandPayload() //
              .setRefId(order.getId())
              .setPickId(context.getPickId()) //
              .setRecipientName(order.getCustomer().getName()) //
              .setRecipientAddress(order.getCustomer().getAddress())) //
        .setCorrelationId(ZeebeWorkarounds.getCorrelationId(taskEvent)));
  }  

}
