package io.flowing.retail.zeebe.payment;

import java.time.Duration;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import io.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.client.api.worker.JobHandler;
import io.zeebe.client.api.worker.JobWorker;

@Component
public class PaymentAdapter {

  @ZeebeWorker(type="retrieve-payment-z")
  public void retrievePayment(JobClient client, ActivatedJob job) {
    try {
      Map<String, Object> variables = job.getVariablesAsMap();
      String traceId = (String) variables.get("traceId");
      String refId = (String) variables.get("refId");
      long amount = (Integer) variables.get("amount");
      
      System.out.println("retrieved payment " + amount + " for " + refId);
    } catch (Exception e) {
      throw new RuntimeException("Could not parse payload: " + e.getMessage(), e);
    }

    client.newCompleteCommand(job.getKey()).send()
      .exceptionally( throwable -> { throw new RuntimeException("Could not complete job " + job, throwable); });
  }

}
