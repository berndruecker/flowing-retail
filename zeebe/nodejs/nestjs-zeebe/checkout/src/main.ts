import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { ZeebeServer } from '@payk/nestjs-zeebe';
import { NestExpressApplication } from '@nestjs/platform-express';

import * as path from 'path';

async function bootstrap() {
  const app = await NestFactory.create<NestExpressApplication>(AppModule);
  app.connectMicroservice({
    strategy: app.get(ZeebeServer),
  });
  await app.startAllMicroservicesAsync();

  app.useStaticAssets(path.join(__dirname, '..', 'public'));
  await app.listen(3000);
}
bootstrap();
