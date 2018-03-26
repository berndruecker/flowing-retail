package io.flowing.retail.payment;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
public class PaymentApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(PaymentApplication.class, args);
  }

}
