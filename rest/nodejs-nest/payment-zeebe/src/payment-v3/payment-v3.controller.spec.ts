import { Test, TestingModule } from '@nestjs/testing';
import { PaymentV3Controller } from './payment-v3.controller';

describe('PaymentV3 Controller', () => {
  let controller: PaymentV3Controller;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PaymentV3Controller],
    }).compile();

    controller = module.get<PaymentV3Controller>(PaymentV3Controller);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
