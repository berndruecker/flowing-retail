package io.flowing.retail.order.port.persistence;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

import org.springframework.stereotype.Component;

import io.flowing.retail.order.domain.Order;

@Component
@Singleton
public class OrderRepository {

  private Map<String, Order> orderStorage =
          new ConcurrentHashMap<>();

  public void createOrder(Order order) {
    order.setId(UUID.randomUUID().toString());
    orderStorage.put(order.getId(), order);
  }

  public void updateOrder(Order order) {
    orderStorage.put(order.getId(), order);
  }

  public Order removeOrder(String orderId) {
    return orderStorage.remove(orderId);
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
