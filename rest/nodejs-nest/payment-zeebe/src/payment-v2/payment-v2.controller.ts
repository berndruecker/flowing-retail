import { Controller, Put } from '@nestjs/common';
import { v4 as uuid } from 'uuid';
import axios from 'axios';
import Brakes from 'brakes';

const stripeChargeUrl = 'http://localhost:8099/charge';

@Controller('/api/payment')
export class PaymentV2Controller {
  @Put('/v2')
  async postPayment() {
    const traceId = uuid();
    const customerId = '0815';
    const amount = 15;

    const brake = new Brakes(this.chargeCreditCard(customerId, amount), {
      timeout: 150,
    });

    await brake.exec();
    return { status: 'completed', traceId };
  }

  async chargeCreditCard(customerId, remainingAmount) {
    const response = await axios.post(stripeChargeUrl, {
      amount: remainingAmount,
      customerId,
    });
    return response.data.transactionId;
  }
}
