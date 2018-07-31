var request = require('request');
const { Client, Variables } = require("camunda-external-task-client-js");

const config = { baseUrl: "http://localhost:8100/rest/engine/default", interval: 50};
const client = new Client(config);

client.subscribe("customer-credit", async function({ task, taskService }) {
  var remainingAmount = 0;
  if (Math.random() > 0.3) {
    remainingAmount = 15;   
  }

  const processVariables = new Variables();
  processVariables.set("remainingAmount", remainingAmount);

  console.log('[%s] done for process instance %s with remainingAmount=%s', task.topicName, task.processInstanceId, remainingAmount);
  await taskService.complete(task, processVariables);
});

client.subscribe("customer-credit-refund", async function({ task, taskService }) {
  console.log('[%s] done for process instance %s', task.topicName, task.processInstanceId);
  await taskService.complete(task);
});
