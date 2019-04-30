package io.flowing.retail.zeebe.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import io.zeebe.client.ZeebeClient;

@SpringBootApplication
public class InventoryApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(InventoryApplication.class, args);
  }

  @Bean
  public ZeebeClient zeebe() {
    // Cannot yet use Spring Zeebe in current alpha
    ZeebeClient zeebeClient = ZeebeClient.newClient();    
    return zeebeClient;
  }

}
