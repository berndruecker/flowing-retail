package io.flowing.retail.order.port.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import javax.inject.Singleton;

import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;

@Component
@Singleton
public class OrderRepository {

  private HashMap<String, Order> orderStorage = new HashMap<String, Order>();

  /**
   * get the order for the specified orderId
   */
  public void persistOrder(Order order) {
    order.setId(UUID.randomUUID().toString());
    orderStorage.put(order.getId(), order);
  }

  /**
   * get the order for the specified orderId
   */
  public Order getOrder(String orderId) {
    return orderStorage.get(orderId);
  }

  /**
   * Find orders. Currently no filter is required for simple examples
   * 
   * @return all stored orders
   */
  public Collection<? extends Order> findOrders() {
    return orderStorage.values();
  }

}
