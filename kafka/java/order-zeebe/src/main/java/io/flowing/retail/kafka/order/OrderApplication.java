package io.flowing.retail.kafka.order;

import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResources = "order-kafka.bpmn")
public class OrderApplication {
  
  public static void main(String[] args) throws Exception {
    SpringApplication.run(OrderApplication.class, args);
  }

}
