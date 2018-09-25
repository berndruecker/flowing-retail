package io.flowing.retail.shipping.messages.payload;

public class CustomerPayload {

  private String name;
  private String address;
  
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getAddress() {
    return address;
  }
  public void setAddress(String address) {
    this.address = address;
  }
}
