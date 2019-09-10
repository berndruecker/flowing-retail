package io.flowing.retail.zeebe.order;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
public class OrderApplication {
  
  @Autowired
  private ZeebeClient zeebeClient;  

  @PostConstruct
  public void deployWorkflowToZeebe() {    
    // Trigger deployment
    zeebeClient.newDeployCommand() //
      .addResourceFromClasspath("order-zeebe.bpmn") //
      .send().join();
  }

  public static void main(String[] args) throws Exception {
    SpringApplication.run(OrderApplication.class, args);
  }

}
