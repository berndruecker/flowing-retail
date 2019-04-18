package io.flowing.retail.kafka.order;

import io.flowing.retail.kafka.order.domain.OrderFlowContext;
import io.zeebe.client.ZeebeClient;

public class SmokeTestZeebe {

	public static void main(String [] args) {
		new SmokeTestZeebe().hardcodedRun();
	}

	private ZeebeClient zeebe;
	
	public void hardcodedRun() {
		
		zeebe = zeebe();
		new RetrievePaymentAdapter().subscribe(zeebe);
		new FetchGoodsAdapter().subscribe(zeebe);
		new ShipGoodsAdapter().subscribe(zeebe);
		
	    OrderFlowContext context = new OrderFlowContext();
	    context.setOrderId("test");
	    context.setTraceId("123");

	    // and kick of a new flow instance
	    System.out.println("New order placed, start flow. " + context.asJson());
	    zeebe.newCreateInstanceCommand() //
	        .bpmnProcessId("order-kafka") //
	        .latestVersion() // 
	        .variables(context.asJson()) //
	        .send().join();

	    
	}
	
	public ZeebeClient zeebe() {
	    // Cannot yet use Spring Zeebe in current alpha
	    ZeebeClient zeebeClient = ZeebeClient.newClient();    
	    
	    // Trigger deployment
	    zeebeClient.newDeployCommand() //
	      .addResourceFromClasspath("order-kafka.bpmn") //
	      .send().join();
	    
	    return zeebeClient;
	  }
}
