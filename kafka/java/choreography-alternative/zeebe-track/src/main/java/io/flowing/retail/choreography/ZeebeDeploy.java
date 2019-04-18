package io.flowing.retail.choreography;

import io.zeebe.client.ZeebeClient;

public class ZeebeDeploy {
  public static void main(String[] args) {
    ZeebeClient zeebe = ZeebeClient.newClient();

    zeebe.newDeployCommand() //
        .addResourceFromClasspath("order-tracking.bpmn") //
        .send().join();

    System.out.println("deployed");

    zeebe.close();
  }
}
