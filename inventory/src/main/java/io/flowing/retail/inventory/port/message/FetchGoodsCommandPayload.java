package io.flowing.retail.inventory.port.message;

import java.util.ArrayList;
import java.util.List;

import io.flowing.retail.inventory.domain.Item;

public class FetchGoodsCommandPayload {
  
  private String refId;
  private String reason = "CustomerOrder";
  private List<Item> items = new ArrayList<>();
  
  public String getRefId() {
    return refId;
  }
  public FetchGoodsCommandPayload setRefId(String refId) {
    this.refId = refId;
    return this;
  }
  public String getReason() {
    return reason;
  }
  public FetchGoodsCommandPayload setReason(String reason) {
    this.reason = reason;
    return this;
  }
  public List<Item> getItems() {
    return items;
  }
  public FetchGoodsCommandPayload setItems(List<Item> items) {
    this.items = items;
    return this;
  }

}
