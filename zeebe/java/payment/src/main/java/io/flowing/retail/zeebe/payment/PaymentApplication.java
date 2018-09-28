package io.flowing.retail.zeebe.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.zeebe.gateway.ZeebeClient;

@SpringBootApplication
public class PaymentApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(PaymentApplication.class, args);
  }

  @Bean
  public ZeebeClient zeebe() {
    ZeebeClient zeebeClient = ZeebeClient.newClient();        
    return zeebeClient;
  }

}
