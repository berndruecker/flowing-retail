import { Controller, Put } from '@nestjs/common';
import { v4 as uuid } from 'uuid';
import axios from 'axios';
import * as Brakes from 'brakes';

const stripeChargeUrl = 'http://localhost:8099/charge';

const brake = new Brakes(this.chargeCreditCard, {
  timeout: 150,
});

@Controller('/api/payment')
export class PaymentV2Controller {
  @Put('/v2')
  async postPayment() {
    const traceId = uuid();
    const customerId = '0815';
    const amount = 15;

    return brake
      .exec({ customerId, amount, traceId })
      .then(() => ({ status: 'completed', traceId }))
      .catch(e => ({ error: e.message }));
  }

  async chargeCreditCard({ customerId, amount, traceId }) {
    const response = await axios.post(stripeChargeUrl, {
      amount,
      customerId,
      traceId,
    });
    return response.data.transactionId;
  }
}
