package io.flowing.retail.order.domain.adapter;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.domain.adapter.base.CommandPubEventSubAdapter;
import io.flowing.retail.order.port.Message;
import io.flowing.retail.order.port.outbound.MessageSender;
import io.flowing.retail.order.repository.OrderRepository;

@Component
public class ShipGoodsAdapter extends CommandPubEventSubAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    Order order = orderRepository.getOrder((String)execution.getVariable("orderId")); 
    String pickId = (String)execution.getVariable("pickId"); // TODO read from step before!
    String traceId = (String)execution.getVariable("traceId"); // Business key?

    addMessageSubscription(execution, "GoodsShippedEvent");
    
    messageSender.send(new Message<ShipGoodsCommandPayload>( //
            "ShipGoodsCommand", //
            new ShipGoodsCommandPayload() //
              .setPickId(pickId) //
              .setRecipientName(order.getCustomer().getName()) //
              .setRecipientAddress(order.getCustomer().getAddress()), //
            traceId));
  }  

}
