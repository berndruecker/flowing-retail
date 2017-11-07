package io.flowing.retail.payment.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.externaltask.CompleteExternalTaskDto;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksDto;
import org.camunda.bpm.engine.rest.dto.externaltask.FetchExternalTasksDto.FetchExternalTaskTopicDto;
import org.camunda.bpm.engine.rest.dto.externaltask.LockedExternalTaskDto;

/**
 * Worker to complete external task "Deduct existing customer credit"
 * used in Payment1
 *
 */
public class CustomerCreditWorker {

  private static final String BASE_URL = "http://localhost:8092/rest/engine/default/";
  private static String WORKER_ID= "someWorker";

  public static void main(String[] args) throws InterruptedException {
    Client client = ClientBuilder.newClient();

    FetchExternalTasksDto fetchExternalTasksDto = new FetchExternalTasksDto();
    fetchExternalTasksDto.setWorkerId(WORKER_ID);
    fetchExternalTasksDto.setMaxTasks(10);

    FetchExternalTaskTopicDto topic1 = new FetchExternalTasksDto.FetchExternalTaskTopicDto();
    topic1.setTopicName("customer-credit");
    topic1.setLockDuration(5000);    
    topic1.setVariables(new ArrayList<String>() {{
      add("payload");
    }});

    FetchExternalTaskTopicDto topic2 = new FetchExternalTasksDto.FetchExternalTaskTopicDto();
    topic2.setTopicName("customer-credit-refund");
    topic2.setLockDuration(5000);    
    topic2.setVariables(new ArrayList<String>() {{
      add("payload");
    }});

    fetchExternalTasksDto.setTopics(new ArrayList<FetchExternalTasksDto.FetchExternalTaskTopicDto>() {{
      add(topic1);
      add(topic2);
    }});

    boolean running=true;
    while (running) {
      List<LockedExternalTaskDto> tasks = client
          .target(BASE_URL + "external-task/fetchAndLock")
          .request(MediaType.APPLICATION_JSON) //
          .post(
              Entity.entity(fetchExternalTasksDto, MediaType.APPLICATION_JSON), //
              new GenericType<List<LockedExternalTaskDto>>() {});
      
      System.out.print(".");
      for (LockedExternalTaskDto task : tasks) {
        if ("customer-credit".equals( task.getTopicName() )) {          
          VariableValueDto remainingAmount = new VariableValueDto();
          remainingAmount.setType("integer");
          remainingAmount.setValue(15);
          
          CompleteExternalTaskDto completeTaskDto = new CompleteExternalTaskDto();
          completeTaskDto.setWorkerId(WORKER_ID);
          completeTaskDto.setVariables(new HashMap<String, VariableValueDto>() {{
            put("remainingAmount", remainingAmount);
          }});
          
          client
            .target("http://localhost:8092/rest/engine/default/external-task")
            .path(task.getId()) //
            .path("complete")
            .request(MediaType.APPLICATION_JSON) //
            .post(
                Entity.entity(completeTaskDto, MediaType.APPLICATION_JSON));
          System.out.println("deducted from customer credit");          
        }
        else if ("customer-credit-refund".equals( task.getTopicName() )) {
          System.out.println("refunded to customer credit");
        }
      }
      
      Thread.sleep(5000);
    }

  
  }
}
