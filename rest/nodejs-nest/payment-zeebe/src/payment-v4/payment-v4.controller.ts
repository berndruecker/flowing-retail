import { Controller, Put, Inject } from '@nestjs/common';
import { v4 as uuid } from 'uuid';
import axios from 'axios';
import * as Brakes from 'brakes';
import { ZBClient } from 'zeebe-node';
import { CompleteFn, Job } from 'zeebe-node';
import { ZEEBE_CONNECTION_PROVIDER, ZeebeWorker } from '@payk/nestjs-zeebe';
import { Ctx, Payload } from '@nestjs/microservices';
import * as path from 'path';
import { Res } from '@nestjs/common';
import { Response } from 'express';
const stripeChargeUrl = 'http://localhost:8099/charge';

const brake = new Brakes(axios.post, {
  timeout: 150,
});

@Controller('/api/payment')
export class PaymentV4Controller {
  constructor(
    @Inject(ZEEBE_CONNECTION_PROVIDER) private readonly zbClient: ZBClient,
  ) {
    this.zbClient.deployWorkflow(
      path.join(__dirname, '..', '..', 'bpmn', 'paymentV4.bpmn'),
    );
  }

  @Put('/v4')
  async postPayment(@Res() res: Response) {
    const traceId = uuid();
    // const customerId = "0815";
    const amount = 15;

    return this.zbClient
      .createWorkflowInstanceWithResult({
        bpmnProcessId: 'paymentV4',
        variables: { amount },
        requestTimeout: 1500,
      })
      .then(() => res.status(200).json({ status: 'completed', traceId }))
      .catch(e =>
        e.message.includes('DEADLINE') || e.message.includes('INTERNAL') // 0.22.1
          ? res.status(202).json({ status: 'pending', traceId }) // Set up callback for worker below
          : res.status(500).json({ error: e.message }),
      );
  }

  @ZeebeWorker('charge-creditcard-v4')
  async chargeCreditCard(
    @Payload() job: Job,
    @Ctx() complete: CompleteFn<any>,
  ) {
    const { amount, traceId } = job.variables;
    const request = { amount, traceId };
    brake
      .exec(stripeChargeUrl, request)
      .then(res =>
        complete.success({ paymentTransactionId: res.transactionId }),
      )
      .catch(() => complete.error('503', 'Service Down'));
  }

  @ZeebeWorker('payment-response-v4')
  paymentResponse(@Payload() job: Job, @Ctx() complete: CompleteFn<any>) {
    // If we returned 202 above, use callback, message, or populate poll endpoint with outcome
    // tslint:disable-next-line: no-console
    // console.log(job);
    complete.success();
  }
}
