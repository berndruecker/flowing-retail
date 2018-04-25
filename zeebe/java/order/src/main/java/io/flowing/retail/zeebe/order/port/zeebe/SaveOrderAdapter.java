package io.flowing.retail.zeebe.order.port.zeebe;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.flowing.retail.zeebe.order.domain.Order;
import io.flowing.retail.zeebe.order.port.persistence.OrderRepository;
import io.zeebe.client.TasksClient;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.spring.client.annotation.ZeebeTaskListener;


@Component
public class SaveOrderAdapter {
  
  @Autowired
  private OrderRepository orderRepository;
  
  @ZeebeTaskListener(taskType = "save-order-z", lockTime=5*60*1000)
  public void retrievePayment(TasksClient zeebe, TaskEvent taskEvent) throws Exception {
    // read data
    JsonObject payload = Json.createReader(new StringReader(taskEvent.getPayload())).readObject();    
    String traceId = payload.getString("traceId");  
    Order order = new ObjectMapper().readValue(payload.getJsonObject("order").toString(), Order.class);
    
    // do something with it
    orderRepository.save(order);
    
    // TODO: Probably improve Json handling, currently mixing javax.json and jackson. Why is JSON handling in Java such a pain? Why?
    JsonReader jsonReader = Json.createReader(new StringReader(new ObjectMapper().writeValueAsString(order)));
    JsonObject orderJson = jsonReader.readObject();
    jsonReader.close();
    
    // write new data
    String payloadNew = Json.createObjectBuilder()
      .add("traceId", traceId) //
      .add("order", orderJson) //
      .build().toString();
    
    // done
    System.out.println("persisted order " + order.getId());
    zeebe.complete(taskEvent).payload(payloadNew).execute();
  }

}
