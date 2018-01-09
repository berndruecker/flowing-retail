package io.flowing.retail.order.port.adapter;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.port.persistence.OrderRepository;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.port.message.Message;
import io.flowing.retail.order.port.message.MessageSender;

@Component
public class OrderCompletedAdapter implements JavaDelegate {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;

  @Override
  public void execute(DelegateExecution context) throws Exception {
    String orderId = (String)context.getVariable("orderId"); 
    String traceId = context.getProcessBusinessKey();

    Order order = orderRepository.removeOrder(orderId);
    if (order == null) {
      throw new IllegalStateException("Unknown order: " + orderId);
    }
    long now = System.currentTimeMillis();
    System.out.println(String.format("Order processed (%s): received: %dms / process-kickoff: %dms / payment: %dms / goods: %dms / ship: %dms / total: %dms",
            order.getId(),
            order.getOrderPlacedRecievedTs() - order.getCreatedTs(),
            order.getRetrievePaymentAdapterTs() - order.getOrderPlacedRecievedTs(),
            order.getFetchGoodsAdapterTs() - order.getRetrievePaymentAdapterTs(),
            order.getShipGoodsAdapterTs() - order.getFetchGoodsAdapterTs(),
            now - order.getShipGoodsAdapterTs(),
            now - order.getCreatedTs()));

    messageSender.send( //
        new Message<OrderCompletedEventPayload>( //
            "OrderCompletedEvent", //
            traceId, //
            new OrderCompletedEventPayload() //
              .setOrderId(orderId)));
  }

  

}
