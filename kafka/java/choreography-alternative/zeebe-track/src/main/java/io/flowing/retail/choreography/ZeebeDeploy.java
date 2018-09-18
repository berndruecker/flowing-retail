package io.flowing.retail.choreography;

import io.zeebe.gateway.ZeebeClient;

public class ZeebeDeploy {
  public static void main(String[] args) {
    ZeebeClient zeebe = ZeebeClient.newClient();

    zeebe.topicClient().workflowClient().newDeployCommand() //
        .addResourceFromClasspath("order-tracking.bpmn") //
        .send().join();

    System.out.println("deployed");

    zeebe.close();
  }
}
