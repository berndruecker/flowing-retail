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
    Semaphore s = semaphors.get(context.getBusinessKey());
    if (s!=null) {
      s.release();
      semaphors.remove(context.getBusinessKey());
    }
  }

  public static Semaphore newSemaphore(String traceId) {
    Semaphore sema = new Semaphore(0);
    semaphors.put(traceId, sema);
    return sema;
  }

  public static void removeSemaphore(String traceId) {
    semaphors.remove(traceId);
  }

}
