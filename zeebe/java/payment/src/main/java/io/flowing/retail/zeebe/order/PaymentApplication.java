package io.flowing.retail.zeebe.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.zeebe.spring.client.*;
import io.zeebe.spring.client.annotation.ZeebeDeployment;

@SpringBootApplication
@EnableZeebeClient
public class PaymentApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(PaymentApplication.class, args);
  }

}
