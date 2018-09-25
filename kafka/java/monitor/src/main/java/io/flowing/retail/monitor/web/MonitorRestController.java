package io.flowing.retail.monitor.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.flowing.retail.monitor.domain.PastEvent;
import io.flowing.retail.monitor.persistence.LogRepository;

@RestController
public class MonitorRestController {
  
  @RequestMapping(path = "/event", method = GET)
  public Map<String, List<PastEvent>> getAllEvents() {
    return LogRepository.instance.getAllPastEvents();    
  }

  @RequestMapping(path = "/event/{traceId}", method = GET)
  public List<PastEvent> getAllEvents(@PathVariable String traceId) {
    return LogRepository.instance.getAllPastEvents(traceId);
  }

}