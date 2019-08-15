package imdb.loader;

import imdb.management.boundary.LoaderMBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.lang.management.ManagementFactory;
import java.util.Date;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchConfiguration {

    @Value("${loader.autostart}")
    private boolean autostart;

    @Value("${loader.concurrency}")
    private Integer concurrencyLimit;
    @Value("${loader.jobs}")
    private Integer jobLimit;

    static final String TITLE_BASICS_URL = "https://datasets.imdbws.com/title.basics.tsv.gz";
    static final String NAME_BASICS_URL = "https://datasets.imdbws.com/name.basics.tsv.gz";
    static final String TITLE_PRINCIPALS_URL = "https://datasets.imdbws.com/title.principals.tsv.gz";
    static final LineTokenizer TOKENIZER = line -> new DefaultFieldSet(Stream.of(line.split("\t")).map(l -> "\\N".equals(l) ? null : l).toArray(String[]::new));

    @Bean
    public GZIPInputStreamFactory gzipInputStreamFactory() {
        return new GZIPInputStreamFactory();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("job_thread_");
        asyncTaskExecutor.setConcurrencyLimit(jobLimit);
        return asyncTaskExecutor;
    }

    @Bean
    public TaskExecutor stepTaskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("step_thread_");
        asyncTaskExecutor.setConcurrencyLimit(concurrencyLimit);
        return asyncTaskExecutor;
    }

    @Bean
    @Primary
    public JobLauncher jobConcurrentLauncher(JobRepository jobRepository) {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setTaskExecutor(taskExecutor());
        simpleJobLauncher.setJobRepository(jobRepository);
        return simpleJobLauncher;
    }

    @Bean
    public ApplicationListener<ContextRefreshedEvent> resumeJobsListener(JobRepository jobRepository, JobExplorer jobExplorer, LoaderMBean loaderMBean) {
        return event -> {
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
                                            log.info("{}: this job is broken and must be reset to STOPPED status", jobName);
                                            execution.setEndTime(new Date());
                                            execution.setStatus(BatchStatus.STOPPED);
                                            execution.setExitStatus(ExitStatus.STOPPED);

                                            for (StepExecution se : execution.getStepExecutions()) {
                                                if (se.getStatus().equals(BatchStatus.STARTED)) {
                                                    log.info("{}: this step execution is broken and must be reset to STOPPED status", se.getStepName());
                                                    se.setEndTime(new Date());
                                                    se.setStatus(BatchStatus.STOPPED);
                                                    se.setExitStatus(ExitStatus.STOPPED);
                                                    jobRepository.update(se);
                                                }
                                            }
                                            jobRepository.update(execution);
                                        }
                                    })));

            if (autostart) {
                log.info("Autostarting loader jobs");
                loaderMBean.startLoader();
            }

        };
    }
}