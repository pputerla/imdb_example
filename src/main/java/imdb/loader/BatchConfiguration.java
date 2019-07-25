package imdb.loader;

import imdb.api.control.ActorMovieRelationRepository;
import imdb.api.control.ActorRepository;
import imdb.api.control.MovieRepository;
import imdb.api.entity.ActorEntity;
import imdb.api.entity.ActorMovieRelationEntity;
import imdb.api.entity.ActorMovieRelationId;
import imdb.api.entity.MovieEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchConfiguration {

    private static final String TITLE_BASICS_URL = "https://datasets.imdbws.com/title.basics.tsv.gz";
    private static final String NAME_BASICS_URL = "https://datasets.imdbws.com/name.basics.tsv.gz";
    private static final String MOVIE_ITEM_READER = "movieItemReader";
    private static final String ACTOR_ITEM_READER = "actorItemReader";
    private static final String IMPORT_MOVIES_JOB = "importMoviesJob";
    private static final String IMPORT_ACTORS_JOB = "importActorsJob";
    private static final String IMPORT_MOVIES_STEP = "importMoviesStep";
    private static final String IMPORT_ACTORS_STEP = "importActorsStep";
    private static final String SAVE_METHOD = "save";
    private static final int CHUNK_SIZE = 10000;
    public static final int CONCURRENCY_LIMIT = 8;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ResourceLoader resourceLoader;


    @Bean
    public GZIPInputStreamFactory gzipInputStreamFactory() {
        return new GZIPInputStreamFactory();
    }


    @Bean
    public FlatFileItemReader<MovieEntity> movieReader(GZIPBufferedReaderFactory gzipBufferedReaderFactory) {
        FlatFileItemReader<MovieEntity> reader = new FlatFileItemReaderBuilder<MovieEntity>()
                .name(MOVIE_ITEM_READER)
                .resource(resourceLoader.getResource(TITLE_BASICS_URL))
                .linesToSkip(1)
                .lineTokenizer(line -> new DefaultFieldSet(Stream.of(line.split("\t")).map(l -> "\\N".equals(l) ? null : l).toArray(String[]::new)))
                .fieldSetMapper(fieldSet -> MovieEntity
                        .builder()
                        .id(Long.parseLong(fieldSet.readString(0).substring(2)))
                        .title(fieldSet.readString(2))
                        .year(Optional.ofNullable(fieldSet.readString(5)).map(Integer::parseInt).orElse(null))
                        .build()
                )
                .build();
        reader.setBufferedReaderFactory(gzipBufferedReaderFactory);
        return reader;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("job_thread_");
        asyncTaskExecutor.setConcurrencyLimit(2);
        return asyncTaskExecutor;
    }


    @Bean
    public TaskExecutor stepTaskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("step_thread_");
        asyncTaskExecutor.setConcurrencyLimit(CONCURRENCY_LIMIT);
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
    public FlatFileItemReader<ActorEntity> actorReader(GZIPBufferedReaderFactory gZipBufferedReaderFactory) {
        FlatFileItemReader<ActorEntity> reader = new FlatFileItemReaderBuilder<ActorEntity>()
                .name(ACTOR_ITEM_READER)
                .resource(resourceLoader.getResource(NAME_BASICS_URL))
                .linesToSkip(1)
                .lineTokenizer(line -> new DefaultFieldSet(Stream.of(line.split("\t")).map(l -> "\\N".equals(l) ? null : l).toArray(String[]::new)))
                .fieldSetMapper(fieldSet -> ActorEntity
                        .builder()
                        .id(Long.parseLong(fieldSet.readString(0).substring(2)))
                        .name(fieldSet.readString(1))
                        .movieIds(createSetOfMovies(fieldSet.readString(5)))
                        .build()
                )
                .build();
        reader.setBufferedReaderFactory(gZipBufferedReaderFactory);
        return reader;
    }

    private List<Long> createSetOfMovies(String readString) {
        if (readString == null) {
            return Collections.emptyList();
        }
        return Stream
                .of(readString.split(","))
                .map(String::trim)
                .map(e -> e.substring(2))
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }


    @Bean
    public RepositoryItemWriter<MovieEntity> movieWriter(MovieRepository movieRepository) {
        return new RepositoryItemWriterBuilder<MovieEntity>()
                .methodName(SAVE_METHOD)
                .repository(movieRepository)
                .build();
    }

    @Bean
    public ItemWriter<ActorEntity> actorWriter(ActorRepository actorRepository, ActorMovieRelationRepository actorMovieRelationRepository) {
        return items -> items
                .forEach(item -> {
                    List<ActorMovieRelationEntity> relations = item
                            .getMovieIds()
                            .stream()
                            .map(movieId -> ActorMovieRelationEntity
                                    .builder()
                                    .actorMovieRelationId(ActorMovieRelationId
                                            .builder()
                                            .actorId(item.getId())
                                            .movieId(movieId)
                                            .build())
                                    .build())
                            .collect(Collectors.toList());

                    actorMovieRelationRepository.saveAll(relations);
                    actorRepository.save(item);
                });
    }


    @Bean
    public Job importMoviesJob(Step importMoviesStep) {
        return jobBuilderFactory.get(IMPORT_MOVIES_JOB)
                .incrementer(parameters -> parameters)
                .flow(importMoviesStep)
                .end()
                .build();
    }

    @Bean
    public Job importActorsJob(Step importActorsStep) {
        return jobBuilderFactory.get(IMPORT_ACTORS_JOB)
                .incrementer(parameters -> parameters)
                .flow(importActorsStep)
                .end()
                .build();
    }

    @Bean
    public Step importMoviesStep(RepositoryItemWriter<MovieEntity> writer, FlatFileItemReader<MovieEntity> reader) {
        return stepBuilderFactory.get(IMPORT_MOVIES_STEP)
                .<MovieEntity, MovieEntity>chunk(CHUNK_SIZE)
                .reader(reader)
                .writer(writer)
                .taskExecutor(stepTaskExecutor())
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(ChunkContext context) {
                        log.info("Movies... processed: {}", context.getStepContext().getStepExecution().getReadCount());
                    }

                    @Override
                    public void afterChunk(ChunkContext context) {

                    }

                    @Override
                    public void afterChunkError(ChunkContext context) {

                    }
                })
                .build();
    }

    @Bean
    public Step importActorsStep(ItemWriter<ActorEntity> writer, FlatFileItemReader<ActorEntity> reader) {
        return stepBuilderFactory.get(IMPORT_ACTORS_STEP)
                .<ActorEntity, ActorEntity>chunk(CHUNK_SIZE)
                .reader(reader)
                .writer(writer)
                .taskExecutor(stepTaskExecutor())
                .listener(new ChunkListener() {
                    @Override
                    public void beforeChunk(ChunkContext context) {
                        log.info("Actors... processed: {}", context.getStepContext().getStepExecution().getReadCount());
                    }

                    @Override
                    public void afterChunk(ChunkContext context) {

                    }

                    @Override
                    public void afterChunkError(ChunkContext context) {

                    }
                })
                .build();
    }


    @Bean
    public ApplicationListener<ContextRefreshedEvent> resumeJobsListener(Set<Job> jobs, JobOperator jobOperator, JobRepository jobRepository,
                                                                         JobExplorer jobExplorer, JobLauncher jobLauncher) {
        // restart jobs that failed due to
        return event -> {
            Date jvmStartTime = new Date(ManagementFactory.getRuntimeMXBean().getStartTime());

            // for each job
            jobExplorer
                    .getJobNames().forEach(jobName -> {
                // get latest job instance
                jobExplorer.getJobInstances(jobName, 0, 1).forEach(instance -> {
                    // for each of the executions
                    jobExplorer.getJobExecutions(instance).forEach(execution -> {
                        if (execution.getStatus().equals(BatchStatus.STARTED) && execution.getCreateTime().before(jvmStartTime)) {
                            // this job is broken and must be restarted
                            execution.setEndTime(new Date());
                            execution.setStatus(BatchStatus.STOPPED);
                            execution.setExitStatus(ExitStatus.STOPPED);

                            for (StepExecution se : execution.getStepExecutions()) {
                                if (se.getStatus().equals(BatchStatus.STARTED)) {
                                    se.setEndTime(new Date());
                                    se.setStatus(BatchStatus.STOPPED);
                                    se.setExitStatus(ExitStatus.STOPPED);
                                    jobRepository.update(se);
                                }
                            }

                            jobRepository.update(execution);
                        }
                    });
                });
            });

            jobs.forEach(job -> {
                try {
                    jobLauncher.run(job, new JobParameters());
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
        };
    }
}