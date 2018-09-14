package io.flowing.retail.payment.application;

import java.util.UUID;

import org.springframework.stereotype.Component;


@Component
public class PaymentService {

  public String createPayment(String orderId, long amount) {
    System.out.println("Create Payment for " + orderId + " with amount "+amount);    
    return UUID.randomUUID().toString();
  }

}
