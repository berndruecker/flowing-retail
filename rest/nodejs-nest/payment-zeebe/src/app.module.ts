import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { PaymentV1Controller } from './payment-v1/payment-v1.controller';
import { PaymentV2Controller } from './payment-v2/payment-v2.controller';
import { PaymentV3Controller } from './payment-v3/payment-v3.controller';
import { PaymentV4Controller } from './payment-v4/payment-v4.controller';
import { ZeebeModule, ZeebeServer } from '@payk/nestjs-zeebe';
import { PaymentV6Controller } from './payment-v6/payment-v6.controller';

@Module({
  imports: [ZeebeModule.forRoot({})],
  controllers: [
    AppController,
    PaymentV1Controller,
    PaymentV2Controller,
    PaymentV3Controller,
    PaymentV4Controller,
    PaymentV6Controller,
  ],
  providers: [AppService, ZeebeServer],
})
export class AppModule {}
