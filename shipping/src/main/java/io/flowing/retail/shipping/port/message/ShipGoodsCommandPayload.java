package io.flowing.retail.shipping.port.message;

public class ShipGoodsCommandPayload {
  
  private String pickId;
  private String logisticsProvider;
  private String recipientName;
  private String recipientAddress;
  
  public String getPickId() {
    return pickId;
  }
  public ShipGoodsCommandPayload setPickId(String pickId) {
    this.pickId = pickId;
    return this;
  }
  public String getLogisticsProvider() {
    return logisticsProvider;
  }
  public ShipGoodsCommandPayload setLogisticsProvider(String logisticsProvider) {
    this.logisticsProvider = logisticsProvider;
    return this;
  }
  public String getRecipientName() {
    return recipientName;
  }
  public ShipGoodsCommandPayload setRecipientName(String recipientName) {
    this.recipientName = recipientName;
    return this;
  }
  public String getRecipientAddress() {
    return recipientAddress;
  }
  public ShipGoodsCommandPayload setRecipientAddress(String recipientAddress) {
    this.recipientAddress = recipientAddress;
    return this;
  }
}
