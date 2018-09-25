package io.flowing.retail.order.flow.base;

import org.camunda.bpm.engine.impl.bpmn.behavior.AbstractBpmnActivityBehavior;
import org.camunda.bpm.engine.impl.event.EventType;
import org.camunda.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

public class PublishSubscribeAdapter extends AbstractBpmnActivityBehavior {
    
  public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
    leave(execution);
  }
  
  protected void addMessageSubscription(final ActivityExecution context, String eventName) {
    ExecutionEntity executionEntity = (ExecutionEntity)context;
    EventSubscriptionEntity eventSubscriptionEntity = new EventSubscriptionEntity(executionEntity, EventType.MESSAGE);
    eventSubscriptionEntity.setEventName(eventName);
    eventSubscriptionEntity.setActivity(executionEntity.getActivity());
    eventSubscriptionEntity.insert();
  }

}