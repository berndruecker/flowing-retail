package io.flowing.retail.kafka.order.adapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.kafka.order.adapter.payload.RetrievePaymentCommandPayload;
import io.flowing.retail.kafka.order.domain.Order;
import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.flowing.retail.kafka.order.port.message.Message;
import io.flowing.retail.kafka.order.port.message.MessageSender;
import io.flowing.retail.kafka.order.port.persistence.OrderRepository;
import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;

@Component
public class PaymentAdapter {
  
  @Autowired
  private MessageSender messageSender;
  
  @Autowired
  private OrderRepository orderRepository;  

  @ZeebeTaskListener(taskType = "retrieve-payment", lockTime=5*60*1000)
  public void sendRetrievePaymentCommand(TasksClient zeebe, TaskEvent taskEvent) throws Exception {
    OrderFlowContext context = OrderFlowContext.fromJson(taskEvent.getPayload());
       
    Order order = orderRepository.findOne(context.getOrderId());   
            
    messageSender.send( //
        new Message<RetrievePaymentCommandPayload>( //
            "RetrievePaymentCommand", //
            context.getTraceId(), //
            new RetrievePaymentCommandPayload() //
              .setRefId(order.getId()) //
              .setReason("order") //
              .setAmount(order.getTotalSum())) //
        .setCorrelationId(ZeebeWorkarounds.getCorrelationId(taskEvent)));
  }

}
