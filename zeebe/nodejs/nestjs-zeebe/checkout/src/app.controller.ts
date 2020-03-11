import { Controller, Get, Put, Param, Inject } from '@nestjs/common';
import { AppService } from './app.service';
import { v4 as uuid } from 'uuid';
import { ZBClient, Job, CompleteFn } from 'zeebe-node';
import { ZEEBE_CONNECTION_PROVIDER, ZeebeWorker } from '@payk/nestjs-zeebe';

@Controller()
export class AppController {
  constructor(
    private readonly appService: AppService,
    @Inject(ZEEBE_CONNECTION_PROVIDER) private readonly zbClient: ZBClient,
  ) {}

  @Put('/api/cart/order')
  async placeOrder(@Param() customerId): Promise<string> {
    const item = {
      articleId: 123,
      amount: 1,
    };

    const customer = {
      name: 'Camunda',
      address: 'Zossener Strasse 55\n10961 Berlin\nGermany',
    };

    const order = {
      items: [item],
      customer,
    };

    const traceId = uuid();

    const payload = {
      traceId,
      order,
    };

    return this.zbClient
      .createWorkflowInstance('order-zeebe', payload)
      .then(() => JSON.stringify({ traceId }));
  }
}
