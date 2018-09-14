package io.flowing.retail.kafka.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zeebe.gateway.ZeebeClient;

@SpringBootApplication
@Configuration
public class OrderApplication {
  
  @Bean
  public ZeebeClient zeebe() {
    // Cannot yet use Spring Zeebe in current alpha
    ZeebeClient zeebeClient = ZeebeClient.newClient();    
    
    // Trigger deployment
    zeebeClient.topicClient().workflowClient().newDeployCommand() //
      .addResourceFromClasspath("order-kafka.bpmn") //
      .send().join();
    
    return zeebeClient;
  }

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext applicationContext = //
        SpringApplication.run(OrderApplication.class, args);    
  }

}
