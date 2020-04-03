import { Controller, Put } from '@nestjs/common';
import { v4 as uuid } from 'uuid';
import axios from 'axios';

const stripeChargeUrl = 'http://localhost:8099/charge';

@Controller('/api/payment')
export class PaymentV1Controller {
  @Put('/v1')
  async putPayment() {
    const traceId = uuid();
    const customerId = '0815';
    const amount = 15;

    await this.chargeCreditCard({ customerId, amount, traceId });
    return { status: 'completed', traceId };
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
