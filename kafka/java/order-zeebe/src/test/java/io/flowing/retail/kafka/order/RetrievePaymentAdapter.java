package io.flowing.retail.kafka.order;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.zeebe.client.ZeebeClient;
import io.zeebe.client.api.clients.JobClient;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.subscription.JobHandler;

@Component
public class RetrievePaymentAdapter implements JobHandler {

	public static String correlationId;
	private ZeebeClient zeebe;

	public void subscribe(ZeebeClient zeebe) {
		this.zeebe = zeebe;
		zeebe.jobClient().newWorker() //
				.jobType("retrieve-payment") //
				.handler(this) //
				.timeout(Duration.ofMinutes(1)) //
				.open();
	}

	@Override
	public void handle(JobClient client, ActivatedJob event) {
		OrderFlowContext context = OrderFlowContext.fromJson(event.getPayload());

		correlationId = UUID.randomUUID().toString();

		client.newCompleteCommand(event.getKey()) //
				.payload(Collections.singletonMap("CorrelationId_RetrievePayment", correlationId)) //
				.send().join();		
		
		zeebe.workflowClient().newPublishMessageCommand() //
				.messageName("PaymentReceivedEvent") //
				.correlationKey(RetrievePaymentAdapter.correlationId) //
				.payload("{\"paymentInfo\": \"YeahWeCouldAddSomething\"}") //
				.send().join();
		
		System.out.println("Correlated PaymentReceivedEvent");
		
	}

}
