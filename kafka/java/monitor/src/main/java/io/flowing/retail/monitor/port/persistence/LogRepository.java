package io.flowing.retail.monitor.port.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flowing.retail.monitor.domain.PastEvent;

public class LogRepository {
  
  public static LogRepository instance = new LogRepository();
  
  private Map<String, List<PastEvent>> events = new HashMap<String, List<PastEvent>>();

  public Map<String, List<PastEvent>> getAllPastEvents() {
    return events;    
  }

  public List<PastEvent> getAllPastEvents(String transactionId) {
    return events.get(transactionId);
  }

  public void addEvent(PastEvent pastEvent) {
    if (!events.containsKey(pastEvent.getTransactionId())) {
      events.put(pastEvent.getTransactionId(), new ArrayList<PastEvent>());
    }
    events.get(pastEvent.getTransactionId()).add(pastEvent);
  }

}
