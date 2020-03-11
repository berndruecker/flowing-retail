import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { ZeebeModule } from '@payk/nestjs-zeebe';

@Module({
  imports: [ZeebeModule.forRoot({ options: { longPoll: 30000 } })],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
