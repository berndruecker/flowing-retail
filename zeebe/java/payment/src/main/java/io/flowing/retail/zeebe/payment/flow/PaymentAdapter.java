package io.flowing.retail.zeebe.payment.flow;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.zeebe.gateway.ZeebeClient;
import io.zeebe.gateway.api.clients.JobClient;
import io.zeebe.gateway.api.events.JobEvent;
import io.zeebe.gateway.api.subscription.JobHandler;
import io.zeebe.gateway.api.subscription.JobWorker;


@Component
public class PaymentAdapter implements JobHandler {
  
  @Autowired
  private ZeebeClient zeebe;

  private JobWorker subscription;
  
  @PostConstruct
  public void subscribe() {
    subscription = zeebe.jobClient().newWorker()
      .jobType("retrieve-payment-z")
      .handler(this)
      .timeout(Duration.ofMinutes(1))
      .open();      
  }

  @Override
  public void handle(JobClient client, JobEvent job) {
    try {
      PaymentInput data = new ObjectMapper().readValue(job.getPayload(), PaymentInput.class);
      String traceId = data.getTraceId();    
      
      String refId = data.getRefId();
      long amount = data.getAmount();
      
      System.out.println("retrieved payment " + amount + " for " + refId);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse payload: " + e.getMessage(), e);
    }

    client.newCompleteCommand(job).send().join();
  }

  @PreDestroy
  public void closeSubscription() {
    subscription.close();      
  }
}
