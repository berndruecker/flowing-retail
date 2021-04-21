package io.flowing.retail.zeebe.order;

import javax.annotation.PostConstruct;

import io.zeebe.spring.client.EnableZeebeClient;
import io.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(classPathResources = "order-zeebe.bpmn")
public class OrderFulfillmentApplication {
  
  public static void main(String[] args) throws Exception {
    SpringApplication.run(OrderFulfillmentApplication.class, args);
  }

}
