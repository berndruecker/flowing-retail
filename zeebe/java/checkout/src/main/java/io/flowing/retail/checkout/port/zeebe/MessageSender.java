package io.flowing.retail.checkout.port.zeebe;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.zeebe.client.ZeebeClient;
import io.zeebe.spring.client.EnableZeebeClient;

@Component
@EnableZeebeClient
public class MessageSender {
  
  @Autowired
  private ZeebeClient zeebe; 
  
  public void send(String payload) {
    try {           
      // start a workflow instance / should be basically just send
      // a message to broker - which will correlate it himself
      // this is not yet in the current tech preview of zeebe - so we 
      // have to specify the workflow to start
      zeebe.workflows().create("default-topic") //
        .bpmnProcessId("order-zeebe") //
        .payload(payload) //
        .execute();
    } catch (Exception e) {
      throw new RuntimeException("Could not tranform and send message due to: "+ e.getMessage(), e);
    }
  }
}
