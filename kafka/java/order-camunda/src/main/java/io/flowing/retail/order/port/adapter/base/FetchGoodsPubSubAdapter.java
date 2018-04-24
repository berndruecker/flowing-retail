package io.flowing.retail.order.port.adapter.base;

import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;
import io.flowing.retail.order.port.adapter.FetchGoodsCommandPayload;
import io.flowing.retail.order.port.message.Message;
import io.flowing.retail.order.port.message.MessageSender;
import io.flowing.retail.order.port.persistence.OrderRepository;

/**
 * Alternative implementation if you prefer having send/receive in one single ServiceTask
 * which is often easier understood by "normal people"
 *
 */
@Component
public class FetchGoodsPubSubAdapter extends PublishSubscribeAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @Override
  public void execute(ActivityExecution context) throws Exception {
    Order order = orderRepository.findOne( //
        (String)context.getVariable("orderId")); 
    String traceId = context.getProcessBusinessKey();

    // publish
    messageSender.send(new Message<FetchGoodsCommandPayload>( //
            "FetchGoodsCommand", //
            traceId, //
            new FetchGoodsCommandPayload() //
              .setRefId(order.getId()) //
              .setItems(order.getItems())));
    
    addMessageSubscription(context, "GoodsFetchedEvent");
  }
  
}
