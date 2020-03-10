package io.flowing.retail.optimize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
public class OptimizeIngestApplication {

  private static Logger log = LoggerFactory.getLogger(OptimizeIngestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(OptimizeIngestApplication.class, args);
    log.debug("Started up ingestion listening to Kafka topic");
	}

  @Bean
  public RestTemplate createRestTemplate() {
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate;
  }

}
