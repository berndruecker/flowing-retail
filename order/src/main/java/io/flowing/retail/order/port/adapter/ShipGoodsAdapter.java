package io.flowing.retail.order.port.adapter;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.port.adapter.base.PublishSubscribeAdapter;
import io.flowing.retail.order.port.message.Message;
import io.flowing.retail.order.port.message.MessageSender;
import io.flowing.retail.order.port.persistence.OrderRepository;

@Component
public class ShipGoodsAdapter extends PublishSubscribeAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @Override
  public void execute(ActivityExecution context) throws Exception {
    Order order = orderRepository.getOrder( //
        (String)context.getVariable("orderId")); 
    String pickId = (String)context.getVariable("pickId"); // TODO read from step before!
    String traceId = context.getProcessBusinessKey();

    messageSender.send(new Message<ShipGoodsCommandPayload>( //
            "ShipGoodsCommand", //
            traceId, //
            new ShipGoodsCommandPayload() //
              .setRefId(order.getId())
              .setPickId(pickId) //
              .setRecipientName(order.getCustomer().getName()) //
              .setRecipientAddress(order.getCustomer().getAddress())));
    
    addMessageSubscription(context, "GoodsShippedEvent");    
  }  

}
