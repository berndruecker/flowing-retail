import { Test, TestingModule } from '@nestjs/testing';
import { PaymentV4Controller } from './payment-v4.controller';

describe('PaymentV4 Controller', () => {
  let controller: PaymentV4Controller;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PaymentV4Controller],
    }).compile();

    controller = module.get<PaymentV4Controller>(PaymentV4Controller);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });
});
