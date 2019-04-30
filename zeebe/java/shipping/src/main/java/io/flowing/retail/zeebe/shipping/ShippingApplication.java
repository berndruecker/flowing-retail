package io.flowing.retail.zeebe.shipping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
public class ShippingApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(ShippingApplication.class, args);
  }

  @Bean
  public ZeebeClient zeebe() {
    ZeebeClient zeebeClient = ZeebeClient.newClient();
    return zeebeClient;
  }

}
