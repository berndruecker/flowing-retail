import { Module, Inject } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { InMemoryDBModule } from '@nestjs-addons/in-memory-db';
import {
  ZeebeServer,
  ZEEBE_CONNECTION_PROVIDER,
  ZeebeModule,
} from '@payk/nestjs-zeebe';
import { ZBClient } from 'zeebe-node';

@Module({
  imports: [
    InMemoryDBModule.forRoot(),
    ZeebeModule.forRoot({ options: { longPoll: 30 } }),
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {
  constructor(
    @Inject(ZEEBE_CONNECTION_PROVIDER) private readonly zbClient: ZBClient,
  ) {
    this.zbClient.deployWorkflow('../bpmn/order-zeebe.bpmn');
  }
}
