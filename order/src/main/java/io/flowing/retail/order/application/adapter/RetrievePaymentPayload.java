package io.flowing.retail.order.application.adapter;

public class RetrievePaymentPayload {
  
  private String refId;
  private String reason;
  private int amount;
  
  public String getRefId() {
    return refId;
  }
  public RetrievePaymentPayload setRefId(String refId) {
    this.refId = refId;
    return this;
  }
  public String getReason() {
    return reason;
  }
  public RetrievePaymentPayload setReason(String reason) {
    this.reason = reason;
    return this;
  }
  public int getAmount() {
    return amount;
  }
  public RetrievePaymentPayload setAmount(int amount) {
    this.amount = amount;
    return this;
  }
}
