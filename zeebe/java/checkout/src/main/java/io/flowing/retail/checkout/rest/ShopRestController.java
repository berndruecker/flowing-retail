package io.flowing.retail.checkout.rest;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.zeebe.gateway.ZeebeClient;

@RestController
public class ShopRestController {
  
  @Autowired
  private ZeebeClient zeebe; 
  
  @RequestMapping(path = "/api/cart/order", method = PUT)
  public String placeOrder(@RequestParam(value = "customerId") String customerId) {
    
    JsonArrayBuilder orderItems = Json.createArrayBuilder();
    
    JsonObjectBuilder item1 = Json.createObjectBuilder();
    item1.add("articleId", "123");
    item1.add("amount", "1");
    orderItems.add(item1);    

    JsonObjectBuilder order = Json.createObjectBuilder();
    order.add("items", orderItems);

    JsonObjectBuilder customer = Json.createObjectBuilder();
    customer.add("name", "Camunda");
    customer.add("address", "Zossener Strasse 55\n10961 Berlin\nGermany");
    order.add("customer", customer);
    
    String traceId = UUID.randomUUID().toString();
    
    String payload = Json.createObjectBuilder() //
      .add("traceId", traceId) //
      .add("order", order).build().toString();
    
    try {           
      // start a workflow instance / should be basically just send
      // a message to broker - which will correlate it himself
      // this is not yet in the current version of zeebe - so we 
      // have to specify the workflow to start
      zeebe.workflowClient().newCreateInstanceCommand() //
        .bpmnProcessId("order-zeebe") //
        .latestVersion() //
        .payload(payload) //
        .send().join();
    } catch (Exception e) {
      throw new RuntimeException("Could not tranform and send message due to: "+ e.getMessage(), e);
    }
        
    // note that we cannot easily return an order id here - as everything is asynchronous
    // and blocking the client is not what we want.
    // but we return an own correlationId which can be used in the UI to show status maybe later
    return "{\"traceId\": \"" + traceId + "\"}";
  }

}