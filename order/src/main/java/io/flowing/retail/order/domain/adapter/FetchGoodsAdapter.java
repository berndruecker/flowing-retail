package io.flowing.retail.order.domain.adapter;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.domain.OrderItem;
import io.flowing.retail.order.domain.adapter.base.CommandPubEventSubAdapter;
import io.flowing.retail.order.port.Message;
import io.flowing.retail.order.port.outbound.MessageSender;
import io.flowing.retail.order.repository.OrderRepository;

@Component
public class FetchGoodsAdapter extends CommandPubEventSubAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    Order order = orderRepository.getOrder((String)execution.getVariable("orderId")); 
    String traceId = (String)execution.getVariable("traceId"); // Business key?

    addMessageSubscription(execution, "GoodsFetchedEvent");
    
    messageSender.send(new Message<FetchGoodsCommandPayload>( //
            "FetchGoodsCommand", //
            new FetchGoodsCommandPayload() //
              .setRefId(order.getId()) //
              .setItems(order.getItems()), //
            traceId));
  }  

  
}
