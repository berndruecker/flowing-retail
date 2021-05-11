package io.flowing.retail.zeebe.shipping;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.stereotype.Component;

@Component
public class ShipGoodsAdapter {

  @ZeebeWorker(type="ship-goods-z")
  public void handle(JobClient client, ActivatedJob job) {
    System.out.println("ship goods");
    client.newCompleteCommand(job.getKey()).send()
      .exceptionally( throwable -> { throw new RuntimeException("Could not complete job " + job, throwable); });
  }

}
