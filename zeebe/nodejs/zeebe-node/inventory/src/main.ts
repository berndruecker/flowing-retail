import { ZBClient } from "zeebe-node";

const zbc = new ZBClient();

zbc.createWorker({
  taskType: "fetch-goods-z",
  taskHandler: (job, complete) => {
    console.log("Fetch goods");
    complete.success();
  }
});
