import { v4 as uuid } from "uuid";
import axios from "axios";
import Brakes from "brakes";
import { ZBClient } from "zeebe-node";
import * as path from "path";

const stripeChargeUrl = "http://localhost:8099/charge";

const zbc = new ZBClient();

zbc.deployWorkflow(path.join(__dirname, "..", "..", "bpmn", "paymentV3.bpmn"));

// /api/payment/v3
export const routev3 = async (req, res) => {
  const traceId = uuid();
  const amount = 15;

  await zbc.createWorkflowInstance("paymentV3", {
    amount,
    traceId
  });

  res.status(202).json({ status: "pending", traceId });
};

const brake = new Brakes(axios.post, {
  timeout: 150
});

zbc.createWorker<{ amount: number; traceId: string }>({
  taskType: "charge-creditcard-v3",
  taskHandler: async (job, complete) => {
    const { traceId, amount } = job.variables;
    const request = { amount, traceId };
    brake
      .exec(stripeChargeUrl, request)
      .then(res => {
        console.log(`Payment processed for ${traceId}`);
        complete.success({ paymentTransactionId: res.transactionId });
      })
      .catch(e => {
        console.log(`Failure for ${traceId}! Sending BPMN Error 503.`);
        complete.error("503", e.message);
      });
  }
});
