package io.flowing.retail.order.port;

public class AcknowledgeResponse {
  private String refId;

  public String getRefId() {
    return refId;
  }

  public void setRefId(String refId) {
    this.refId = refId;
  }
}
