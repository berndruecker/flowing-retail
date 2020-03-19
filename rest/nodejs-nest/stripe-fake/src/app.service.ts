import { Injectable } from '@nestjs/common';
import { v4 as uuid } from 'uuid';

@Injectable()
export class AppService {
  public slow = false;
  chargeCreditCard(traceId: string): Promise<any> {
    const waitTimeMillis = this.slow
      ? Math.floor(Math.random() * 60 * 1000)
      : 0;

    // tslint:disable-next-line: no-console
    console.log(`Charge for ${traceId}`);
    // tslint:disable-next-line: no-console
    console.log(
      `Charge on credit card will take ${waitTimeMillis / 1000} seconds`,
    );

    const response =
      Math.random() > 0.8
        ? { transactionId: uuid(), errorCode: 'credit card expired', traceId }
        : { transactionId: uuid(), traceId };

    return new Promise(resolve =>
      setTimeout(() => resolve(response), waitTimeMillis),
    );
  }
}
