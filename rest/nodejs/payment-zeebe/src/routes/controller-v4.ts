import { v4 as uuid } from "uuid";
import axios from "axios";
import Brakes from "brakes";
import { ZBClient } from "zeebe-node";
import * as path from "path";

const stripeChargeUrl = "http://localhost:8099/charge";

const zbc = new ZBClient();

zbc.deployWorkflow(path.join(__dirname, "..", "..", "bpmn", "paymentV4.bpmn"));

// /api/payment/v4
export const routev4 = async (req, res) => {
  const traceId = uuid();
  // const customerId = "0815";
  const amount = 15;

  return zbc
    .createWorkflowInstanceWithResult({
      bpmnProcessId: "paymentV4",
      variables: { amount },
      requestTimeout: 500
    })
    .then(() => ({ status: "completed", traceId }))
    .catch(e => {
      if (e.message.includes("DEADLINE")) {
        return { status: "pending", traceId };
      } else {
        throw e;
      }
    });
};

zbc.createWorker<{ amount: number; traceId: string }>({
  taskType: "charge-creditcard-v4",
  taskHandler: async (job, complete) => {
    const { amount, traceId } = job.variables;
    const request = { amount, traceId };
    const brake = new Brakes(axios.post(stripeChargeUrl, request), {
      timeout: 150
    });
    const res = await brake.exec();
    complete.success({ paymentTransactionId: res.transactionId });
  }
});

zbc.createWorker<{ amount: number }>({
  taskType: "payment-response-v4",
  taskHandler: async (job, complete) => {
    complete.success();
  }
});
