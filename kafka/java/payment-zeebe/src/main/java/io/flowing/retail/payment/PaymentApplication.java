package io.flowing.retail.payment;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableZeebeClient
@ZeebeDeployment(resources = "classpath:payment.bpmn")
public class PaymentApplication {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(PaymentApplication.class, args);
  }

  @Bean
  public RestTemplate createRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate;
  }
}
