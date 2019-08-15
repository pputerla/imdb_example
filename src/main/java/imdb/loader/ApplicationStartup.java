package imdb.loader;

import imdb.management.boundary.LoaderMBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationStartup {

    private final JobRepository jobRepository;
    private final JobExplorer jobExplorer;
    private final LoaderMBean loaderMBean;
    @Value("${loader.autostart}")
    private boolean autostart;

    @EventListener(ApplicationStartedEvent.class)
    public void resumeJobsListener() {

        Date jvmStartTime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());

        jobExplorer
                .getJobNames()
                .forEach(jobName -> jobExplorer
                        .getJobInstances(jobName, 0, 1)
                        .forEach(instance -> jobExplorer
                                .getJobExecutions(instance)
                                .forEach(execution -> {
                                    if (execution.getStatus().equals(BatchStatus.STARTED) && execution.getCreateTime().before(jvmStartTime)) {
                                        //
                                        log.info("jobExecutionId={}, jobName={}: this job execution is broken and must be reset to STOPPED status", execution.getId(), jobName);
                                        execution.setEndTime(new Date());
                                        execution.setStatus(BatchStatus.STOPPED);
                                        execution.setExitStatus(ExitStatus.STOPPED);

                                        for (StepExecution se : execution.getStepExecutions()) {
                                            if (se.getStatus().equals(BatchStatus.STARTED)) {
                                                log.info("stepName={}, stepId={}, jobName={}: this step execution is broken and must be reset to STOPPED status", se.getStepName(), se.getId(), jobName);
                                                se.setEndTime(new Date());
                                                se.setStatus(BatchStatus.STOPPED);
                                                se.setExitStatus(ExitStatus.STOPPED);
                                                jobRepository.update(se);
                                            }
                                        }
                                        jobRepository.update(execution);
                                    }
                                })));


    }

    @EventListener(ApplicationReadyEvent.class)
    public void autoStartJobs() {
        if (autostart) {
            log.info("Autostarting loader job");
            loaderMBean.startLoader();
        }
    }
}