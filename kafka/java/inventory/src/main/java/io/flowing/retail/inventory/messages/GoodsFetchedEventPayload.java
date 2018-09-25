package io.flowing.retail.inventory.messages;

public class GoodsFetchedEventPayload {
  
  private String refId;
  private String pickId;

  public String getRefId() {
    return refId;
  }

  public GoodsFetchedEventPayload setRefId(String refId) {
    this.refId = refId;
    return this;
  }

  public String getPickId() {
    return pickId;
  }

  public GoodsFetchedEventPayload setPickId(String pickId) {
    this.pickId = pickId;
    return this;
  }
}
