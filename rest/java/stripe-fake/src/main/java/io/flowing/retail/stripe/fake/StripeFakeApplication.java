package io.flowing.retail.stripe.fake;

import java.util.Scanner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StripeFakeApplication {

  public static void main(String[] args) {
    SpringApplication.run(StripeFakeApplication.class, args);
    
    try {
      System.out.println("Service is operating normal");
  
      Scanner scanner = new Scanner(System.in);
  
      while (true) {
        System.out.print("[S]low, [N]ormal: ");
        String mode = scanner.next().toUpperCase();
        if ("S".equals(mode)) {
          StripeFakeRestController.slow = true;
          System.out.println("Service is now slow");
        }
        else if ("N".equals(mode)) {
          StripeFakeRestController.slow = false;
          System.out.println("Service is back to normal");
        }
      }
    }
    catch (Exception ex) {
      // silently ignore
    }
  }

}
