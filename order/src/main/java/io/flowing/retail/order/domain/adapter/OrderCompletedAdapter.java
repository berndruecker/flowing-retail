package io.flowing.retail.order.domain.adapter;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.port.Message;
import io.flowing.retail.order.port.outbound.MessageSender;
import io.flowing.retail.order.repository.OrderRepository;

@Component
public class OrderCompletedAdapter implements JavaDelegate {
  
  @Autowired
  private MessageSender messageSender;  

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String orderId = (String)execution.getVariable("orderId"); 
    String traceId = (String)execution.getVariable("traceId"); // Business key?

    messageSender.send( //
        new Message<OrderCompletedEventPayload>( //
            "OrderCompletedEvent", //
            new OrderCompletedEventPayload() //
              .setOrderId(orderId), //
            traceId));
  }

  

}
