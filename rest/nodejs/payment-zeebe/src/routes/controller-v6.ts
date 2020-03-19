import { v4 as uuid } from "uuid";
import axios from "axios";
import Brakes from "brakes";
import { ZBClient } from "zeebe-node";
import * as path from "path";

const stripeChargeUrl = "http://localhost:8099/charge";

const zbc = new ZBClient();

zbc.deployWorkflow(path.join(__dirname, "..", "..", "bpmn", "paymentV6.bpmn"));

// /api/payment/v4
export const routev6 = async (_, res) => {
  const traceId = uuid();
  const customerId = "0815";
  const amount = 15;

  return zbc
    .createWorkflowInstanceWithResult({
      bpmnProcessId: "paymentV6",
      variables: { amount, customerId, traceId },
      requestTimeout: 1500
    })
    .then(() => res.json({ status: "completed", traceId }))
    .catch(e => {
      if (e.message.includes("DEADLINE") || e.message.includes("INTERNAL")) {
        // set up some state to communicate the outcome in the worker below
        res.status(202).json({ status: "pending", traceId });
      } else {
        res.status(500).json({ error: e.toString() });
      }
    });
};

const brake = new Brakes(axios.post, {
  timeout: 150
});

zbc.createWorker<{ amount: number; traceId: string }>({
  taskType: "charge-creditcard-v6",
  taskHandler: async (job, complete) => {
    const { amount, traceId } = job.variables;
    const request = { amount, traceId };
    brake
      .exec(stripeChargeUrl, request)
      .then(res =>
        complete.success({ paymentTransactionId: res.transactionId })
      )
      .catch(e => complete.error("503", "Service unavailable"));
  }
});

zbc.createWorker<{ amount: number }>({
  taskType: "customer-credit-refund-v6",
  taskHandler: async (_, complete) => {
    // Here is where you credit the customer account in the database
    complete.success({
      remainingAmount: 15,
      amount: 15,
      credit: 0
    });
  }
});

zbc.createWorker<{ amount: number }>({
  taskType: "customer-credit-v6",
  taskHandler: async (job, complete) => {
    // Here you get the customer credit from a database, decrement it
    // and apply the credit to the purchase
    const credit = Math.floor(Math.random() * 16);
    complete.success({
      remainingAmount: job.variables.amount - credit,
      credit
    });
  }
});

zbc.createWorker<{ amount: number }>({
  taskType: "payment-response-v6",
  taskHandler: async (job, complete) => {
    // You need to implement a callback mechanism here to communicate
    // the outcome if the sync response didn't pan out
    complete.success();
  }
});
