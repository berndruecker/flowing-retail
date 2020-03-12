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
    amount
  });

  return { status: "pending", traceId };
};

zbc.createWorker<{ amount: number }>({
  taskType: "charge-creditcard-v3",
  taskHandler: async (job, complete) => {
    const request = { amount: job.variables.amount };
    const brake = new Brakes(axios.post(stripeChargeUrl, request), {
      timeout: 150
    });
    const res = await brake.exec();
    complete.success({ paymentTransactionId: res.transactionId });
  }
});
