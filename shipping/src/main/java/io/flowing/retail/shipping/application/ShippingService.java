package io.flowing.retail.shipping.application;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class ShippingService {

  @Value("${flowing-retail.verbose}")
  private boolean verbose;

  /**
   * 
   * @param pick id - required to identify the pile of goods to be packed in the parcel
   * @param recipient name
   * @param complete address the shipment is sent to
   * @param logisticsProvider delivering the shipment (e.g. DHL, UPS, ...)
   * @return shipment id created (also printed on the label of the parcel)
   */
  public String createShipment(String pickId, String recipientName, String recipientAddress, String logisticsProvider) {
    if (verbose) {
      System.out.println("Shipping to " + recipientName + "\n\n" + recipientAddress);
    }
    
    return UUID.randomUUID().toString();
  }

}
