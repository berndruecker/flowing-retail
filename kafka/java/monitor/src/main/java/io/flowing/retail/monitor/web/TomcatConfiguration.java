package io.flowing.retail.monitor.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TomcatConfiguration implements WebMvcConfigurer {
  
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
      // forward requests index.htm (as we might have two spring boot tomcats running in the same JVM they can see each others resources
      // so we use different index files to avoid confusion
      registry.addViewController("/").setViewName(
          "forward:/monitor.html");
  }
  
}
