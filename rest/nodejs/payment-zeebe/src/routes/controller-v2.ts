import { v4 as uuid } from "uuid";
import axios from "axios";
import Brakes from "brakes";

const stripeChargeUrl = "http://localhost:8099/charge";

// /api/payment/v2
export const routev2 = async (req, res) => {
  const traceId = uuid();
  const customerId = "0815";
  const amount = 15;

  const brake = new Brakes(chargeCreditCard(customerId, amount), {
    timeout: 150
  });

  return brake.exec().then(() => res.json({ status: "completed", traceId }));
};

async function chargeCreditCard(customerId, remainingAmount) {
  const response = await axios.post(stripeChargeUrl, {
    amount: remainingAmount,
    customerId
  });
  return response.data.transactionId;
}
