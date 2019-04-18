package io.flowing.retail.kafka.order;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
@Configuration
public class OrderApplication {
  
  @Value("${zeebe.brokerContactPoint}")
  private String zeebeBrokerContactPoint;
  
  @Bean
  public ZeebeClient zeebe() {
    System.out.println("Connect to Zeebe at '" + zeebeBrokerContactPoint + "'");
    
    // Cannot yet use Spring Zeebe in current alpha
    ZeebeClient zeebeClient = ZeebeClient.newClientBuilder() //
        .brokerContactPoint(zeebeBrokerContactPoint) //
        .build();
    
    // Trigger deployment
    zeebeClient.newDeployCommand() //
      .addResourceFromClasspath("order-kafka.bpmn") //
      .send().join();
    
    return zeebeClient;
  }

  public static void main(String[] args) throws Exception {
    ConfigurableApplicationContext applicationContext = //
        SpringApplication.run(OrderApplication.class, args);    
  }

}
