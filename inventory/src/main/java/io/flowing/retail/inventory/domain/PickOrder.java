package io.flowing.retail.inventory.domain;

import java.util.List;
import java.util.UUID;

public class PickOrder {

  private String pickId = UUID.randomUUID().toString(); 
  private List<Item> items;
  
  public String getPickId() {
    return pickId;
  }
  public PickOrder setPickId(String pickId) {
    this.pickId = pickId;
    return this;
  }
  public List<Item> getItems() {
    return items;
  }
  public PickOrder setItems(List<Item> items) {
    this.items = items;
    return this;
  }
}
