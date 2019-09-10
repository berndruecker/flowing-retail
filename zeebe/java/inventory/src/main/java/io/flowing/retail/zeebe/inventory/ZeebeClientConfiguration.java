package io.flowing.retail.zeebe.inventory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.zeebe.client.ZeebeClient;

@Configuration
public class ZeebeClientConfiguration {
  
  @Bean
  public ZeebeClient zeebe() {
    ZeebeClient zeebeClient = ZeebeClient.newClient();        
    return zeebeClient;
  }

}
