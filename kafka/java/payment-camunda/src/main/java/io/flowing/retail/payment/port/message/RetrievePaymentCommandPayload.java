package io.flowing.retail.payment.port.message;

public class RetrievePaymentCommandPayload {
  
  private String refId;
  private String reason;
  private int amount;
  
  public String getRefId() {
    return refId;
  }
  public RetrievePaymentCommandPayload setRefId(String refId) {
    this.refId = refId;
    return this;
  }
  public String getReason() {
    return reason;
  }
  public RetrievePaymentCommandPayload setReason(String reason) {
    this.reason = reason;
    return this;
  }
  public int getAmount() {
    return amount;
  }
  public RetrievePaymentCommandPayload setAmount(int amount) {
    this.amount = amount;
    return this;
  }
}
