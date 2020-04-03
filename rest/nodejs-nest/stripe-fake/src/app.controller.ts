import { Controller, Get, Post, Body } from '@nestjs/common';
import { AppService } from './app.service';
import * as keypress from 'keypress';

keypress(process.stdin);

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {
    process.stdin.on('keypress', (ch, key) => {
      if (!key) {
        return;
      }
      if (key.name === 'c' && key.ctrl) {
        process.exit();
      }
      if (key.name === 's') {
        // tslint:disable-next-line: no-console
        console.log('Service is now slow');
        appService.slow = true;
      }
      if (key.name === 'n') {
        // tslint:disable-next-line: no-console
        console.log('Service is back to normal');
        appService.slow = false;
      }
    });

    process.stdin.setRawMode(true);
    process.stdin.resume();
  }

  @Post('/charge')
  getHello(@Body() body): Promise<any> {
    return this.appService.chargeCreditCard(body.traceId);
  }
}
