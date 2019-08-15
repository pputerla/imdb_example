package imdb.management.boundary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ManagedResource(
        objectName = "imdb:category=Loader,name=control",
        description = "User Hit Count")
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class LoaderMBean {


    private final Set<Job> jobs;
    private final JobLauncher jobLauncher;
    private Set<JobExecution> jobExecutions = new HashSet<>();

    @ManagedOperation
    public void startLoader() {
        jobs
                .forEach(job -> {
                    try {
                        log.info("Launching job {}", job.getName());
                        jobExecutions.add(jobLauncher.run(job, new JobParameters()));
                    } catch (JobExecutionAlreadyRunningException e) {
                        log.warn("Job is alreday running ({})", e.getMessage());
                    } catch (JobRestartException e) {
                        log.error("Problem with job, could not run: {}", e.getMessage());
                    } catch (JobInstanceAlreadyCompleteException e) {
                        log.info("Job {} completed: {}", job.getName(), e.getMessage());
                    } catch (JobParametersInvalidException e) {
                        log.error("Invalid job parameters: {}", e.getMessage());
                    }
                });
    }

    @ManagedOperation
    public void stopLoader() {
        log.info("Stopping all jobs");
        jobExecutions.forEach(JobExecution::stop);
        jobExecutions.clear();
    }


}