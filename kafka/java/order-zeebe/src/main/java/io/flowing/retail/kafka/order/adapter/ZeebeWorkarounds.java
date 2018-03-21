package io.flowing.retail.kafka.order.adapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.clustering.impl.BrokerPartitionState;
import io.zeebe.client.event.TaskEvent;
import io.zeebe.client.event.TopicSubscription;
import io.zeebe.client.task.cmd.CompleteTaskCommand;

public class ZeebeWorkarounds {

  public static String getCorrelationId(TaskEvent taskEvent) {
    // currently it is not yet implemented in Zeebe that you can easily complete
    // a task soley by its id.
    // so as a workaround I remember the position in the log
    // which allows to read back the task event required to complete the task
    long positionInLog = taskEvent.getMetadata().getPosition();
    // to make the workaround smell really bad I misuse the traceId to transport
    // the positionId
    String correlationId = String.valueOf(positionInLog);
    return correlationId;
  }

  public static CompleteTaskCommand createCompleteTaskCommandByCorrelationId(ZeebeClient zeebe, String correlationId) {
    try {
      TaskEvent taskEvent = readBackTaskEvent(zeebe, Long.parseLong( correlationId ));
      return zeebe.tasks().complete(taskEvent);
    } catch (Exception e) {
      throw new RuntimeException("Could not complete task in Zeebe: " + e.getMessage(), e);
    }
  }


  public static TaskEvent readBackTaskEvent(ZeebeClient zeebe, long correlationInfoPositionInLog)
      throws InterruptedException, ExecutionException, TimeoutException {
    CompletableFuture<TaskEvent> future = new CompletableFuture<>();

    TopicSubscription subscription = zeebe.topics() //
        .newSubscription("default-topic") //
        .name("taskEvent-lookup") //
        .forcedStart() //
        .startAtPosition(getPartitionIdOfTopic(zeebe), correlationInfoPositionInLog - 1) //
        .taskEventHandler(event -> { //
          if (event.getMetadata().getPosition() == correlationInfoPositionInLog) {
            future.complete(event);
          }
        }).open();
    TaskEvent taskEvent = future.get(10, TimeUnit.SECONDS);
    subscription.close();

    return taskEvent;
  }

  public static int getPartitionIdOfTopic(ZeebeClient zeebe) {
    String topic = "default-topic";
    return zeebe.requestTopology().execute() //
        .getBrokers() //
        .stream() //
        .flatMap(b -> b.getPartitions().stream()) //
        .filter(p -> p.getTopicName().equals(topic)) //
        .findFirst() //
        .map(BrokerPartitionState::getPartitionId) //
        .orElseThrow(() -> new RuntimeException("Doesn't find topic with name: " + topic));
  }
}
