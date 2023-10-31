package io.flowing.retail.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
public class PaymentRestApplication {

  @Bean
  public ZeebeClient zeebe() {
    ZeebeClient zeebeClient = ZeebeClient.newClientBuilder() //
    		.usePlaintext() // don't use SSL in default local broker
    		.build();
    return zeebeClient;
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(PaymentRestApplication.class, args);
  }

}
