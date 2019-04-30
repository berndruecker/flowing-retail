package io.flowing.retail.zeebe.payment.flow;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.subscription.JobHandler;
import io.zeebe.client.api.subscription.JobWorker;


@Component
public class PaymentAdapter implements JobHandler {
  
  @Autowired
  private ZeebeClient zeebe;

  private JobWorker subscription;
  
  @PostConstruct
  public void subscribe() {
    subscription = zeebe.newWorker()
      .jobType("retrieve-payment-z")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, ActivatedJob job) {
    try {
      String traceId = (String) job.getVariablesAsMap().get("traceId");          
      String refId = (String) job.getVariablesAsMap().get("refId");
      long amount = (Integer) job.getVariablesAsMap().get("amount");
      
      System.out.println("retrieved payment " + amount + " for " + refId);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse payload: " + e.getMessage(), e);
    }

    client.newCompleteCommand(job.getKey()).send().join();
  }

  @PreDestroy
  public void closeSubscription() {
    subscription.close();      
  }
}
