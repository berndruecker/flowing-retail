package io.flowing.retail.checkout.port.web.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Controller
public class TomcatConfiguration {
  
  @Bean
  public WebMvcConfigurerAdapter forwardToIndex() {
    return new WebMvcConfigurerAdapter() {
      @Override
      public void addViewControllers(ViewControllerRegistry registry) {
        // forward requests index.htm (as we might have two spring boot tomcats
        // running in the same JVM they can see each others resources
        // so we use different index files to avoid confusion
        registry.addViewController("/").setViewName("forward:/shop.html");
      }
    };
  }

}
