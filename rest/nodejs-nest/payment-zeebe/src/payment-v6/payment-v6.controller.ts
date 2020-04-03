import { Controller, Inject, Put, Res } from '@nestjs/common';
import { ZEEBE_CONNECTION_PROVIDER, ZeebeWorker } from '@payk/nestjs-zeebe';
import { ZBClient, Job, CompleteFn } from 'zeebe-node';
import { Payload, Ctx } from '@nestjs/microservices';
import { v4 as uuid } from 'uuid';
import * as path from 'path';
import { Response } from 'express';
import * as Brakes from 'brakes';
import axios from 'axios';

const stripeChargeUrl = 'http://localhost:8099/charge';

const brake = new Brakes(axios.post, {
  timeout: 150,
});

@Controller('/api/payment')
export class PaymentV6Controller {
  constructor(
    @Inject(ZEEBE_CONNECTION_PROVIDER) private readonly zbClient: ZBClient,
  ) {
    this.zbClient.deployWorkflow(
      path.join(__dirname, '..', '..', 'bpmn', 'paymentV6.bpmn'),
    );
  }

  @Put('/v6')
  async postPayment(@Res() res: Response) {
    const traceId = uuid();
    const customerId = '0815';
    const amount = 15;

    return this.zbClient
      .createWorkflowInstanceWithResult({
        bpmnProcessId: 'paymentV6',
        variables: { amount, customerId, traceId },
        requestTimeout: 500,
      })
      .then(() => res.status(200).json({ status: 'completed', traceId }))
      .catch(e =>
        e.message.includes('DEADLINE') || e.message.includes('INTERNAL') // 0.22.1
          ? res.status(202).json({ status: 'pending', traceId }) // Set up callback for worker below
          : res.status(500).json({ error: e.message }),
      );
  }

  @ZeebeWorker('charge-creditcard-v6')
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

  @ZeebeWorker('customer-credit-refund-v6')
  refund(@Payload() _: Job, @Ctx() complete: CompleteFn<any>) {
    // Here is where you credit the customer account in the database
    complete.success({
      remainingAmount: 15,
      amount: 15,
      credit: 0,
    });
  }

  @ZeebeWorker('customer-credit-v6')
  chargeAccount(@Payload() job: Job, @Ctx() complete: CompleteFn<any>) {
    // Here you get the customer credit from a database, decrement it
    // and apply the credit to the purchase
    const credit = Math.floor(Math.random() * 16);
    complete.success({
      remainingAmount: job.variables.amount - credit,
      credit,
    });
  }

  @ZeebeWorker('payment-response-v6')
  paymentResponse(@Payload() _: Job, @Ctx() complete: CompleteFn<any>) {
    // If we returned 202 above, use callback, message, or populate poll endpoint with outcome
    complete.success();
  }
}
