package io.flowing.retail.order.domain;

import javax.annotation.PostConstruct;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import io.flowing.retail.order.port.adapter.FetchGoodsAdapter;
import io.flowing.retail.order.port.adapter.OrderCompletedAdapter;
import io.flowing.retail.order.port.adapter.RetrievePaymentAdapter;
import io.flowing.retail.order.port.adapter.ShipGoodsAdapter;

@Configuration
public class OrderFulfillmentFlowDefinition {

  @Autowired
  private ProcessEngine engine;
  
  @Autowired 
  private ApplicationContext applicationContext;
  
  @PostConstruct
  public void createFlow() {
    engine.getRepositoryService().createDeployment() //
      .addModelInstance("order.bpmn", Bpmn.createProcess("order").executable() //
        .startEvent().message("OrderPlacedEvent")
        .serviceTask().name("Retrieve payment").camundaDelegateExpression(exp(RetrievePaymentAdapter.class))
        .serviceTask().name("Fetch goods").camundaDelegateExpression(exp(FetchGoodsAdapter.class))
          .camundaOutputParameter("pickId", "#{PAYLOAD_GoodsFetchedEvent.jsonPath('$.pickId').stringValue()}")
        .serviceTask().name("Ship goods").camundaDelegateExpression(exp(ShipGoodsAdapter.class))
        .endEvent().camundaExecutionListenerDelegateExpression("end", exp(OrderCompletedAdapter.class))
        .done()
      ).deploy();
  }
  
  public String exp(Class delegateClass) {
    String[] beanNames = applicationContext.getBeanNamesForType(delegateClass);
    if (beanNames.length>1) {
      throw new RuntimeException("More than one Spring bean found for type " + delegateClass);
    }
    return "#{" + beanNames[0] + "}";
  }
  
 }
