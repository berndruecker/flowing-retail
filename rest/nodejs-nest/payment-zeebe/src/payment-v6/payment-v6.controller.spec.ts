import { Test, TestingModule } from '@nestjs/testing';
import { PaymentV6Controller } from './payment-v6.controller';

describe('PaymentV6 Controller', () => {
  let controller: PaymentV6Controller;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PaymentV6Controller],
    }).compile();

    controller = module.get<PaymentV6Controller>(PaymentV6Controller);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
