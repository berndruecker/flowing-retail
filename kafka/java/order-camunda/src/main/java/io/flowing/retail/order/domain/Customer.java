package io.flowing.retail.order.domain;

public class Customer {
  
  private String name;
  private String address;
  
  public String getName() {
    return name;
  }
  public Customer setName(String name) {
    this.name = name;
    return this;
  }
  public String getAddress() {
    return address;
  }
  public Customer setAddress(String address) {
    this.address = address;
    return this;
  }

}
