package io.flowing.retail.zeebe.order;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResources = "order-zeebe.bpmn")
public class OrderFulfillmentApplication {
  
  public static void main(String[] args) throws Exception {
    SpringApplication.run(OrderFulfillmentApplication.class, args);
  }

}
