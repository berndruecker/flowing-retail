package io.flowing.retail.optimize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
@EnableBinding(Sink.class)
public class MessageSinkToOptimizeIngestListener {

  private static Logger log = LoggerFactory.getLogger(MessageSinkToOptimizeIngestListener.class);
  
  @Value("${camunda.optimize.ingestion.endpoint:http://localhost:8090/api/ingestion/event/batch}")
  private String optimizeIngestionEndpoint;
  @Value("${camunda.optimize.ingestion.accessToken}")
  private String optimizeIngestionAccessToken;  

  @Autowired
  private RestTemplate rest;

  @Autowired
  private ObjectMapper objectMapper;

  @StreamListener(target = Sink.INPUT)
  public void messageReceived(String messageJson) throws Exception { 

    // Build array with exactly this one event
    ArrayNode messageArray = objectMapper.createArrayNode();    
    messageArray.add(objectMapper.readTree(messageJson));
    
    // and send it over to Optimize
    sendCloudEventsToOptimize(messageArray.toString());
    
    // An optimization could be to collect events and send them as batch
    // if you have high loads
  }

  public void sendCloudEventsToOptimize(String messageArrayJsonString) {
    log.debug("Try to ingest event into Optimize\n"+messageArrayJsonString);

    // prepare request
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set(HttpHeaders.AUTHORIZATION, optimizeIngestionAccessToken);
    HttpEntity<String> request = new HttpEntity<String>(messageArrayJsonString, headers);
    
    try {      
      // Use Optimize Event Ingestion API, see https://docs.camunda.org/optimize/latest/technical-guide/event-ingestion-rest-api/
      ResponseEntity<String> response = rest.postForEntity( //
          optimizeIngestionEndpoint, //
          request, //
          String.class);

      if (response.getStatusCodeValue()==204) {
        log.debug("Ingested event into Optimize\nMessages:"+messageArrayJsonString+"\nResponse:"+response);
      } else {
        // Actually errors should be lead to exceptions in Spring already - but just to be sure! 
        throw new IllegalArgumentException("Could not ingest event into Optimize, response code: " + response.getStatusCodeValue());        
      }
    } catch (Exception ex) {
      // Just log the problem and move on      
      log.error("Could not ingest event into Optimize\n"+messageArrayJsonString, ex);
      // This leads to this event being missing in optimize
      // but I don't care for this demo and prefer to move on when something is wired
      // Not the best real-life strategy!
    }    
  }


}
