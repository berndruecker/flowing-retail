package io.flowing.retail.checkout;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.zeebe.gateway.ZeebeClient;

@SpringBootApplication
public class CheckoutApplication {

  public static void main(String[] args) {
    SpringApplication.run(CheckoutApplication.class, args);
  }

  @Bean
  public ZeebeClient zeebe() {
    ZeebeClient zeebeClient = ZeebeClient.newClient();    
    return zeebeClient;
  }

}
