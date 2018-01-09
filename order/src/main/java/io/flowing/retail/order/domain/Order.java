package io.flowing.retail.order.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Order {

  protected String id = UUID.randomUUID().toString();
  protected Customer customer = new Customer(); 
  protected List<OrderItem> items = new ArrayList<OrderItem>();
  private long createdTs;
  private long orderPlacedRecievedTs;
  private long retrievePaymentAdapterTs;
  private long fetchGoodsAdapterTs;
  private long shipGoodsAdapterTs;

  public void addItem(OrderItem i) {
    items.add(i);
  }
  
  public int getTotalSum() {
    int sum = 0;
    for (OrderItem orderItem : items) {
      sum += orderItem.getAmount();
    }
    return sum;
  }
  
  public String getId() {
    return id;
  }

  @JsonProperty("orderId")
  public void setId(String id) {
    this.id = id;
  }

  public List<OrderItem> getItems() {
    return items;
  }

  public long getCreatedTs() {
    return createdTs;
  }

  public void setCreatedTs(long createdTs) {
    this.createdTs = createdTs;
  }

  public long getOrderPlacedRecievedTs() {
    return orderPlacedRecievedTs;
  }

  public void setOrderPlacedRecievedTs(long orderPlacedRecievedTs) {
    this.orderPlacedRecievedTs = orderPlacedRecievedTs;
  }

  public long getRetrievePaymentAdapterTs() {
    return retrievePaymentAdapterTs;
  }

  public void setRetrievePaymentAdapterTs(long retrievePaymentAdapterTs) {
    this.retrievePaymentAdapterTs = retrievePaymentAdapterTs;
  }

  public long getFetchGoodsAdapterTs() {
    return fetchGoodsAdapterTs;
  }

  public void setFetchGoodsAdapterTs(long fetchGoodsAdapterTs) {
    this.fetchGoodsAdapterTs = fetchGoodsAdapterTs;
  }

  public long getShipGoodsAdapterTs() {
    return shipGoodsAdapterTs;
  }

  public void setShipGoodsAdapterTs(long shipGoodsAdapterTs) {
    this.shipGoodsAdapterTs = shipGoodsAdapterTs;
  }

  @Override
  public String toString() {
    return "Order [id=" + id + ", items=" + items + "]";
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }


  
}
