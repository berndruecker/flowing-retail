import { v4 as uuid } from "uuid";
import axios from "axios";

const stripeChargeUrl = "http://localhost:8099/charge";

// /api/payment/v1
export const routev1 = async (req, res) => {
  const traceId = uuid();
  const customerId = "0815";
  const amount = 15;

  await chargeCreditCard(customerId, amount);
  res.json({ status: "completed", traceId });
};

async function chargeCreditCard(customerId, remainingAmount) {
  const response = await axios.post(stripeChargeUrl, {
    amount: remainingAmount,
    customerId
  });
  return response.data.transactionId;
}
