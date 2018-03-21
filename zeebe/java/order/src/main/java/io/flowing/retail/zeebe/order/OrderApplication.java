package io.flowing.retail.zeebe.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.zeebe.spring.client.*;
import io.zeebe.spring.client.annotation.ZeebeDeployment;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResource = "order-zeebe.bpmn")
public class OrderApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(OrderApplication.class, args);
  }

}
