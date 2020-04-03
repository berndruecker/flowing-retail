import { v4 as uuid } from "uuid";

// Stripe API calls have an idempotency key, so that is handled
export const fakeStripeController = {
  slow: false,
  handler: (req, res) => {
    const { traceId } = req.body;

    const waitTimeMillis = fakeStripeController.slow
      ? Math.floor(Math.random() * 60 * 1000)
      : 0;

    console.log(`Charge for ${traceId}`);
    console.log(
      `Charge on credit card will take ${waitTimeMillis / 1000} seconds`
    );

    const response =
      Math.random() > 0.8
        ? { transactionId: uuid(), errorCode: "credit card expired", traceId }
        : { transactionId: uuid(), traceId };

    return setTimeout(() => res.json(response), waitTimeMillis);
  }
};
