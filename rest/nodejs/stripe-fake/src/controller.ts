import { v4 as uuid } from "uuid";

export const fakeStripeController = {
  slow: false,
  handler: (req, res) => {
    const waitTimeMillis = fakeStripeController.slow
      ? Math.floor(Math.random() * 60 * 1000)
      : 0;

    console.log(`Charge for ${req.body.traceId}`);
    console.log(
      `Charge on credit card will take ${waitTimeMillis / 1000} seconds`
    );

    const response =
      Math.random() > 0.8
        ? { transactionId: uuid(), errorCode: "credit card expired" }
        : { transactionId: uuid() };

    setTimeout(() => res.json(response), waitTimeMillis);
  }
};
