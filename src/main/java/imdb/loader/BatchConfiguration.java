package imdb.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.item.file.transform.DefaultFieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.util.stream.Stream;

@Configuration
@EnableBatchProcessing
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BatchConfiguration {

    static final String TITLE_BASICS_URL = "https://datasets.imdbws.com/title.basics.tsv.gz";
    static final String NAME_BASICS_URL = "https://datasets.imdbws.com/name.basics.tsv.gz";
    static final String TITLE_PRINCIPALS_URL = "https://datasets.imdbws.com/title.principals.tsv.gz";
    static final LineTokenizer TOKENIZER = line -> new DefaultFieldSet(Stream.of(line.split("\t")).map(l -> "\\N".equals(l) ? null : l).toArray(String[]::new));
    private static final String LOADER_JOB = "loaderJob";
    private final JobBuilderFactory jobBuilderFactory;
    @Value("${loader.concurrency}")
    private Integer concurrencyLimit;

    @Bean
    public GZIPInputStreamFactory gzipInputStreamFactory() {
        return new GZIPInputStreamFactory();
    }

    @Bean
    public TaskExecutor stepTaskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor("step_thread_");
        asyncTaskExecutor.setConcurrencyLimit(concurrencyLimit);
        return asyncTaskExecutor;
    }

    @Bean
    public Job loaderJob(Step importActorsStep, Step importMoviesStep, Step importActorsMoviesRelationStep) {
        return jobBuilderFactory
                .get(LOADER_JOB)
                .incrementer(parameters -> parameters)
                .flow(importMoviesStep)
                .next(importActorsStep)
                .next(importActorsMoviesRelationStep)
                .end()
                .build();
    }

}