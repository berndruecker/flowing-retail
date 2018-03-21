package io.flowing.retail.kafka.order.adapter.payload;

import java.util.ArrayList;
import java.util.List;

import io.flowing.retail.kafka.order.domain.OrderItem;

public class FetchGoodsCommandPayload {
  
  private String refId;
  private String reason = "CustomerOrder";
  private List<OrderItem> items = new ArrayList<>();
  
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
  public List<OrderItem> getItems() {
    return items;
  }
  public FetchGoodsCommandPayload setItems(List<OrderItem> items) {
    this.items = items;
    return this;
  }

}
