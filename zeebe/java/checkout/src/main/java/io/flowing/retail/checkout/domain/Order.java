package io.flowing.retail.checkout.domain;

import java.util.ArrayList;
import java.util.List;

public class Order {
  
  private String orderId;
  private Customer customer;
  private List<Item> items = new ArrayList<>();
  
  public void addItem(String articleId, int amount) {
    // keep only one item, but increase amount accordingly
    Item existingItem = removeItem(articleId);
    if (existingItem!=null) {
      amount += existingItem.getAmount();
    }
    
    Item item = new Item();
    item.setAmount(amount);
    item.setArticleId(articleId);
    items.add(item);    
  }

  public Item removeItem(String articleId) {
    for (Item item : items) {
      if (articleId.equals(item.getArticleId())) {
        items.remove(item);
        return item;
      }
    }
    return null;
  }
  
  public int getTotalSum() {
    int sum = 0;
    for (Item item : items) {
      sum += item.getAmount();
    }
    return sum;
  }
  
  public String getOrderId() {
    return orderId;
  }
  public void setOrderId(String orderId) {
    this.orderId = orderId;
  }
  public List<Item> getItems() {
    return items;
  }
  public void setItems(List<Item> items) {
    this.items = items;
  }
  @Override
  public String toString() {
    return "Order [orderId=" + orderId + ", items=" + items + "]";
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }
}
