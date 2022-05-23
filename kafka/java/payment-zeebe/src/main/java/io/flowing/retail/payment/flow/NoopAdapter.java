package io.flowing.retail.payment.flow;

import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import org.springframework.stereotype.Component;

@Component
public class NoopAdapter {

  @ZeebeWorker(type = "noop", autoComplete = true)
  public void doNothing() {
  }
}
