import { Controller, Put, Inject } from '@nestjs/common';
import { v4 as uuid } from 'uuid';
import axios from 'axios';
import Brakes from 'brakes';
import { ZBClient } from 'zeebe-node';
import { CompleteFn, Job } from 'zeebe-node';
import { ZEEBE_CONNECTION_PROVIDER, ZeebeWorker } from '@payk/nestjs-zeebe';
import { Ctx, Payload } from '@nestjs/microservices';
import * as path from 'path';

const stripeChargeUrl = 'http://localhost:8099/charge';

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
  async postPayment() {
    const traceId = uuid();
    // const customerId = "0815";
    const amount = 15;

    return this.zbClient
      .createWorkflowInstanceWithResult({
        bpmnProcessId: 'paymentV4',
        variables: { amount },
        requestTimeout: 500,
      })
      .then(() => ({ status: 'completed', traceId }))
      .catch(e => {
        if (e.message.includes('DEADLINE')) {
          return { status: 'pending', traceId };
        } else {
          throw e;
        }
      });
  }

  @ZeebeWorker('charge-creditcard-v4')
  async chargeCreditCard(
    @Payload() job: Job,
    @Ctx() complete: CompleteFn<any>,
  ) {
    const { amount, traceId } = job.variables;
    const request = { amount, traceId };
    const brake = new Brakes(axios.post(stripeChargeUrl, request), {
      timeout: 150,
    });
    const res = await brake.exec();
    complete.success({ paymentTransactionId: res.transactionId });
  }

  @ZeebeWorker('payment-response-v4')
  paymentResponse(@Payload() _: Job, @Ctx() complete: CompleteFn<any>) {
    complete.success();
  }
}
