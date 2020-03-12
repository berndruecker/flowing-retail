import express from "express";
import { fakeStripeController } from "./controller";
import keypress from "keypress";

keypress(process.stdin);

const app = express();
const port = 8099;

app.post("/charge", fakeStripeController.handler);

process.stdin.on("keypress", function(ch, key) {
  if (!key) {
    return;
  }
  if (key.name == "c" && key.ctrl) {
    process.exit();
  }
  if (key.name == "s") {
    console.log("Service is now slow");
    fakeStripeController.slow = true;
  }
  if (key.name == "n") {
    console.log("Service is back to normal");
    fakeStripeController.slow = false;
  }
});

process.stdin.setRawMode(true);
process.stdin.resume();

app.listen(port, () => {
  console.log(`Fake Stripe REST API listening on port ${port}`);
  console.log("[S]low, [N]ormal");
});
