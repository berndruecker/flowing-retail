const { Client, Variables } = require("camunda-external-task-client-js");

const config = { baseUrl: "http://localhost:8080/engine-rest/engine/default/", interval: 50};
const client = new Client(config);

client.subscribe("customer-credit", async function({ task, taskService }) {
  const remainingAmount = (Math.random() > 0.3) ? 15 : 0;

  const processVariables = new Variables();
  processVariables.set("remainingAmount", remainingAmount);

  console.log('[%s] done for process instance %s with remainingAmount=%s', task.topicName, task.processInstanceId, remainingAmount);
  await taskService.complete(task, processVariables);
});

client.subscribe("customer-credit-refund", async function({ task, taskService }) {
  console.log('[%s] done for process instance %s', task.topicName, task.processInstanceId);
  await taskService.complete(task);
});
