package io.flowing.retail.shipping.port.message.payload;

public class GoodsFetchedEventPayload {
  
  private String pickId;
  private CustomerPayload customer;
  
  public String getPickId() {
    return pickId;
  }

  public CustomerPayload getCustomer() {
    return customer;
  }

  public void setPickId(String pickId) {
    this.pickId = pickId;
  }

  public void setCustomer(CustomerPayload customer) {
    this.customer = customer;
  }

}
