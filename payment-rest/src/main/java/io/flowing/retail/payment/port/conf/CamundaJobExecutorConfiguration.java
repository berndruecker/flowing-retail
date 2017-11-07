package io.flowing.retail.payment.port.conf;

import org.camunda.bpm.engine.impl.jobexecutor.CallerRunsRejectedJobsHandler;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.spring.components.jobexecutor.SpringJobExecutor;
import org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.impl.DefaultJobConfiguration.JobConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class CamundaJobExecutorConfiguration extends DefaultJobConfiguration {

  @Bean
  @ConditionalOnProperty(prefix = "camunda.bpm.job-execution", name = "enabled", havingValue = "true", matchIfMissing = true)  
  public static JobExecutor jobExecutor(@Qualifier(JobConfiguration.CAMUNDA_TASK_EXECUTOR_QUALIFIER) final TaskExecutor taskExecutor) {
    final SpringJobExecutor springJobExecutor = new SpringJobExecutor();
    springJobExecutor.setTaskExecutor(taskExecutor);
    springJobExecutor.setRejectedJobsHandler(new CallerRunsRejectedJobsHandler());
    
    springJobExecutor.setWaitTimeInMillis(10);
//    springJobExecutor.setWaitIncreaseFactor(1.0f);
    springJobExecutor.setMaxWait(20);
    
    return springJobExecutor;
  }

}