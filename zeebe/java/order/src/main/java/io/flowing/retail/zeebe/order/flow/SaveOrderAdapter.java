package io.flowing.retail.zeebe.order.flow;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.flowing.retail.zeebe.order.domain.Order;
import io.flowing.retail.zeebe.order.persistence.OrderRepository;
import io.zeebe.gateway.ZeebeClient;
import io.zeebe.gateway.api.clients.JobClient;
import io.zeebe.gateway.api.events.JobEvent;
import io.zeebe.gateway.api.subscription.JobHandler;
import io.zeebe.gateway.api.subscription.JobWorker;


@Component
public class SaveOrderAdapter implements JobHandler {
  
  @Autowired
  private OrderRepository orderRepository;
  
  @Autowired
  private ZeebeClient zeebe;

  private JobWorker subscription;
  
  @PostConstruct
  public void subscribe() {
    subscription = zeebe.jobClient().newWorker()
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
  public void handle(JobClient client, JobEvent job) {
    // read data
    OrderFlowContext context = OrderFlowContext.fromJson(job.getPayload());
    Order order = context.getOrder();
    
    // do something with it
    orderRepository.save(order);
    
    // TODO: double chekc that his is updated without (call by reference i think)
    //context.setOrder(order);
    
     // done
    System.out.println("persisted order " + order.getId());
    client.newCompleteCommand(job) //
      .payload(context.asJson()) //
      .send().join();
  }

}
