package io.flowing.retail.monitor.web;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.flowing.retail.monitor.domain.PastEvent;
import io.flowing.retail.monitor.port.persistence.LogRepository;

@RestController
public class MonitorRestController {
  
  @RequestMapping(path = "/event", method = GET)
  public Map<String, List<PastEvent>> getAllEvents() {
    return LogRepository.instance.getAllPastEvents();    
  }

}