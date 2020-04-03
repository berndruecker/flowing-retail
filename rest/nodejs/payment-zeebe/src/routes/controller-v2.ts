import { v4 as uuid } from "uuid";
import axios from "axios";
import Brakes from "brakes";

const stripeChargeUrl = "http://localhost:8099/charge";

const brake = new Brakes(chargeCreditCard, {
  timeout: 150
});

// /api/payment/v2
export const routev2 = async (_, res) => {
  const traceId = uuid();
  const customerId = "0815";
  const amount = 15;

  brake
    .exec({ customerId, amount, traceId })
    .then(() => res.json({ status: "completed", traceId }))
    .catch(e => res.status(503).json({ error: e.message }));
};

async function chargeCreditCard({ amount, customerId, traceId }) {
  const response = await axios.post(stripeChargeUrl, {
    amount,
    customerId,
    traceId
  });
  return response.data.transactionId;
}
