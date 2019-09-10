package io.flowing.retail.zeebe.order.flow;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.zeebe.order.domain.Order;
import io.flowing.retail.zeebe.order.persistence.OrderRepository;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;


@Component
public class SaveOrderAdapter implements JobHandler {
  
  @Autowired
  private OrderRepository orderRepository;
  
  @Autowired
  private ZeebeClient zeebe;

  private JobWorker subscription;
  
  @PostConstruct
  public void subscribe() {
    subscription = zeebe.newWorker()
      .jobType("save-order-z")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @PreDestroy
  public void closeSubscription() {
    subscription.close();      
  }
  
  @Override
  public void handle(JobClient client, ActivatedJob job) {
    OrderFlowContext context = OrderFlowContext.fromJson(job.getVariables());
    Order order = context.getOrder();
    
    // do something with it
    orderRepository.save(order);
    
    // TODO: double chekc that his is updated without (call by reference i think)
    //context.setOrder(order);
    
     // done
    System.out.println("persisted order " + order.getId());
    client.newCompleteCommand(job.getKey()) //
      .variables(context.asJson()) //
      .send().join();
  }
}
