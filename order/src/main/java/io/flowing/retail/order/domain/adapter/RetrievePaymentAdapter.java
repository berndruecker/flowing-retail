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
public class RetrievePaymentAdapter extends CommandPubEventSubAdapter {
  
  @Autowired
  private MessageSender messageSender;  

  @Autowired
  private OrderRepository orderRepository;  

  @Override
  public void execute(ActivityExecution execution) throws Exception {
    Order order = orderRepository.getOrder( //
        (String)execution.getVariable("orderId")); 

    addMessageSubscription(execution, "PaymentReceivedEvent");
    
    messageSender.send( //
        new Message<RetrievePaymentCommandPayload>( //
            "RetrievePaymentCommand", //
            new RetrievePaymentCommandPayload() //
              .setRefId(order.getId()) //
              .setAmount(order.getTotalSum()), //
            execution.getBusinessKey()));
  }

}
