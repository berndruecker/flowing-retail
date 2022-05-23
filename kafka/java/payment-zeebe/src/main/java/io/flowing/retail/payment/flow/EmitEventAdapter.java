package io.flowing.retail.payment.flow;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.ZeebeWorker;
import io.flowing.retail.payment.messages.Message;
import io.flowing.retail.payment.messages.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmitEventAdapter {

    @Autowired
    private MessageSender messageSender;

    @ZeebeWorker(type = "emitEvent", autoComplete = true)
    public void emitEvent(ActivatedJob job) throws Exception {
        String traceId = (String) job.getVariablesAsMap().get("traceId");
        String eventName = (String) job.getCustomHeaders().get("eventName");

        messageSender.send( //
                new Message<String>( //
                        eventName, //
                        traceId, //
                        null)); // no payload at the moment
    }

}