package io.flowing.retail.order.application.adapter;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.domain.OrderRepository;
import io.flowing.retail.order.port.Message;
import io.flowing.retail.order.port.MessageSender;

@Component
public class OrderCompletedAdapter implements JavaDelegate {
  
  @Autowired
  private MessageSender messageSender;  

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    String orderId = (String)execution.getVariable("orderId"); 
    String traceId = (String)execution.getVariable("traceId"); // Business key?

    messageSender.send( //
        new Message<OrderCompletedEvent>( //
            "OrderCompletedEvent", //
            new OrderCompletedEvent() //
              .setOrderId(orderId), //
            traceId));
  }

  public static class OrderCompletedEvent {
    private String orderId;
    public String getOrderId() {
      return orderId;
    }
    public OrderCompletedEvent setOrderId(String orderId) {
      this.orderId = orderId;
      return this;
    }
  }

}
