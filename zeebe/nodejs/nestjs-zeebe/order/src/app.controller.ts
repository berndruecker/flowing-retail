import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';
import { ZeebeWorker } from '@payk/nestjs-zeebe';
import { Job, CompleteFn } from 'zeebe-node';
import {
  InMemoryDBService,
  InMemoryDBEntity,
} from '@nestjs-addons/in-memory-db';
import { v4 as uuid } from 'uuid';

@Controller()
export class AppController {
  constructor(private readonly orderService: InMemoryDBService<OrderEntity>) {}

  @ZeebeWorker('save-order-2', { timeout: 60 })
  saveOrder(job: Job, complete: CompleteFn<any>): void {
    const order = {
      ...job.variables.order,
      orderId: uuid(),
    };
    this.orderService.create(order);
    complete.success({ order });
  }
}

interface OrderEntity extends InMemoryDBEntity {
  id: number;
  orderId: string;
  order: Array<>;
  traceId: string;
}
