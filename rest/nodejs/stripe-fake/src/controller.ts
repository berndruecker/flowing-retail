import { v4 as uuid } from "uuid";

export const fakeStripeController = {
  slow: false,
  handler: (_, res) => {
    const waitTimeMillis = fakeStripeController.slow
      ? 0
      : Math.floor(Math.random() * 60 * 1000);

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
