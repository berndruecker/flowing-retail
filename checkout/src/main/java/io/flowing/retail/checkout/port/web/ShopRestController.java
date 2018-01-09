package io.flowing.retail.checkout.port.web;

import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.flowing.retail.checkout.domain.Customer;
import io.flowing.retail.checkout.domain.Order;
import io.flowing.retail.checkout.port.message.Message;
import io.flowing.retail.checkout.port.message.MessageSender;

import java.util.concurrent.TimeUnit;

@RestController
public class ShopRestController {

  @Autowired
  private MessageSender messageSender;

  @Value("${flowing-retail.verbose}")
  private boolean verbose;

  @RequestMapping(path = "/api/cart/order", method = PUT)
  public String placeOrder(@RequestParam(value = "customerId") String customerId) {
    
    Order order = new Order();
    order.setCreatedTs(System.currentTimeMillis());
    order.addItem("article1", 5);
    order.addItem("article2", 10);
    
    order.setCustomer(new Customer("Camunda", "Zossener Strasse 55\n10961 Berlin\nGermany"));
    
    Message<Order> message = new Message<Order>("OrderPlacedEvent", order);
    messageSender.send(message);
        
    // note that we cannot easily return an order id here - as everything is asynchronous
    // and blocking the client is not what we want.
    // but we return an own correlationId which can be used in the UI to show status maybe later
    return "{\"traceId\": \"" + message.getTraceId() + "\"}";
  }

  @RequestMapping(path = "/api/cart/order-many", method = PUT)
  public String placeOrders(@RequestParam(value = "customerId") String customerId, @RequestParam(value = "numOrders") int numOrders) {
    long start = System.nanoTime();
    for (int i = 0; i < numOrders; i++) {
      placeOrder(customerId);
    }
    long end = System.nanoTime();
    if (verbose) {
      System.out.println("Placed " + numOrders + " orders in " + TimeUnit.NANOSECONDS.toMillis(end - start) + " ms");
    }
    return Integer.toString(numOrders);
  }
}