package io.flowing.retail.shipping.messages;

public class GoodsShippedEventPayload {
  
  private String refId;
  private String shipmentId;

  public String getRefId() {
    return refId;
  }

  public GoodsShippedEventPayload setRefId(String refId) {
    this.refId = refId;
    return this;
  }

  public String getShipmentId() {
    return shipmentId;
  }

  public GoodsShippedEventPayload setShipmentId(String shipmentId) {
    this.shipmentId = shipmentId;
    return this;
  }
}
