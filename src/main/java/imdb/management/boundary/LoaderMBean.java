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
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@ManagedResource(
        objectName = "imdb:category=Loader,name=control",
        description = "Loader Management Bean")
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class LoaderMBean {


    private final Job loaderJob;
    private final JobLauncher jobLauncher;
    private JobExecution jobExecution;

    @ManagedOperation
    public void startLoader() {

        try {
            log.info("Launching job {}", loaderJob.getName());
            jobExecution = jobLauncher.run(loaderJob, new JobParameters());
        } catch (JobExecutionAlreadyRunningException e) {
            log.warn("Job is already running ({})", e.getMessage());
        } catch (JobRestartException e) {
            log.error("Problem with job, could not run: {}", e.getMessage());
        } catch (JobInstanceAlreadyCompleteException e) {
            log.info("Job {} completed: {}", loaderJob.getName(), e.getMessage());
        } catch (JobParametersInvalidException e) {
            log.error("Invalid job parameters: {}", e.getMessage());
        }

    }

    @ManagedOperation
    public void stopLoader() {
        log.info("Stopping loader job");
        Optional.ofNullable(jobExecution).ifPresent(JobExecution::stop);
    }


}