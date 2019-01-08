package io.flowing.retail.payment.resthacks.worker;

import java.util.Collections;

import org.camunda.bpm.client.ExternalTaskClient;

/**
 * Worker to complete external task "Deduct existing customer credit" used in
 * PaymentV5, PaymentV6 and PaymentV7
 *
 */
public class CustomerCreditWorker {

  private static final String BASE_URL = "http://localhost:8100/rest/engine/default/";

  public static void main(String[] args) throws InterruptedException {
    // bootstrap the client
    ExternalTaskClient client = ExternalTaskClient.create() //
        .baseUrl(BASE_URL) //
        .asyncResponseTimeout(5000) // long polling interval
        .build();

    // subscribe to the topic
    client.subscribe("customer-credit") //
      .lockDuration(5000) //
      .handler((externalTask, externalTaskService) -> {

      // retrieve a variable from the Workflow Engine
      long amount = externalTask.getVariable("amount");

      // complete the external task
      externalTaskService.complete(externalTask, Collections.singletonMap("remainingAmount", 15));

      System.out.println("deducted " + amount + " from customer credit");
    }).open();

    client.subscribe("customer-credit-refund") //
      .lockDuration(5000) // 
      .handler((externalTask, externalTaskService) -> {
      externalTaskService.complete(externalTask);
      System.out.println("refunded to customer credit");
    }).open();

  }
}
