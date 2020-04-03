import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  await app.listen(8099);
  // tslint:disable-next-line: no-console
  console.log(`Fake Stripe REST API listening on port 8099`);
  // tslint:disable-next-line: no-console
  console.log('[S]low, [N]ormal');
}
bootstrap();
