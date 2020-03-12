import { Test, TestingModule } from '@nestjs/testing';
import { PaymentV2Controller } from './payment-v2.controller';

describe('PaymentV2 Controller', () => {
  let controller: PaymentV2Controller;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PaymentV2Controller],
    }).compile();

    controller = module.get<PaymentV2Controller>(PaymentV2Controller);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
