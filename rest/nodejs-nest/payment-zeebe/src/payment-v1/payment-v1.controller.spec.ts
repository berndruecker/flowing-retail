import { Test, TestingModule } from '@nestjs/testing';
import { PaymentV1Controller } from './payment-v1.controller';

describe('PaymentV1 Controller', () => {
  let controller: PaymentV1Controller;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PaymentV1Controller],
    }).compile();

    controller = module.get<PaymentV1Controller>(PaymentV1Controller);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
