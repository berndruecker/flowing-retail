package io.flowing.retail.zeebe.shipping;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableZeebeClient
public class ShippingApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(ShippingApplication.class, args);
  }

}
