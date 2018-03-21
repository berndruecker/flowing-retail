package io.flowing.retail.order;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication
public class OrderApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(OrderApplication.class, args);
  }
}
