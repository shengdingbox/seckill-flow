package com.seckillflow.config;


import com.seckillflow.core.job.LogClearJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationStartup implements ApplicationListener {

    @Autowired
    private Scheduler scheduler;
    private static final String JOB_NAME = "clean-log";

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(event instanceof ApplicationReadyEvent) {
            try {
                JobDetail jobDetailInStore = scheduler.getJobDetail(new JobKey("clean-log", "DEFAULT"));
                if(jobDetailInStore == null) {
                    JobDetail jobDetail = JobBuilder.newJob(LogClearJob.class).withIdentity("clean-log", "DEFAULT").storeDurably(true).build();
                    Trigger trigger = TriggerBuilder.newTrigger().startNow().withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInHours(1).repeatForever()).build();
                    scheduler.scheduleJob(jobDetail, trigger);
                }
                log.info("定时清理日志任务启动完成");
            } catch(SchedulerException e) {
                log.error("", e);
            }
        }
    }
}
