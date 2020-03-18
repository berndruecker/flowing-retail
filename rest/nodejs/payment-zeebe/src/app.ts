import express from "express";
import { routev1, routev2, routev3, routev4 } from "./routes";
const app = express();
const port = 8100;

app.put("/api/payment/v1", routev1);
app.put("/api/payment/v2", routev2);
app.put("/api/payment/v3", routev3);
app.put("/api/payment/v4", routev4);

app.listen(port, () =>
  console.log(`Zeebe Payment REST API listening on port ${port}`)
);
