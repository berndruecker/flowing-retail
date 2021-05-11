package io.flowing.retail.zeebe.order.flow;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.flowing.retail.zeebe.order.domain.Order;
import io.flowing.retail.zeebe.order.persistence.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SaveOrderAdapter {
  
  @Autowired
  private OrderRepository orderRepository;

  @ZeebeWorker(type="save-order-z")
  public void saveOrder(JobClient client, ActivatedJob job) {
    OrderFlowContext context = OrderFlowContext.fromJson(job.getVariables());
    Order order = context.getOrder();
    
    // do something with it
    orderRepository.save(order);
    
    // TODO: double check that his is updated without the following statement (call by reference I think)
    //context.setOrder(order);
    
     // done
    System.out.println("persisted order " + order.getId());
    client.newCompleteCommand(job.getKey()) //
      .variables(context.asJson()) //
      .send()
      .exceptionally( throwable -> { throw new RuntimeException("Could not complete job " + job, throwable); });
  }
}
