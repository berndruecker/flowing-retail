package io.flowing.retail.payment.port.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
public class NotifySemaphorAdapter implements JavaDelegate {
  
  public static Map<String, Semaphore> semaphors = new HashMap<>();

  @Override
  public void execute(DelegateExecution context) throws Exception {
    semaphors.get(context.getBusinessKey()).release();
  }

  public static Semaphore newSemaphore(String traceId) {
    Semaphore sema = new Semaphore(0);
    semaphors.put(traceId, sema);
    return sema;
  }

}
