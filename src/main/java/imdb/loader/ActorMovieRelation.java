package imdb.loader;

import imdb.api.control.ActorMovieRelationRepository;
import imdb.api.entity.ActorMovieRelationEntity;
import imdb.api.entity.ActorMovieRelationId;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.task.TaskExecutor;

import static imdb.loader.BatchConfiguration.TITLE_PRINCIPALS_URL;
import static imdb.loader.BatchConfiguration.TOKENIZER;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ActorMovieRelation {


    private static final String ACTOR_MOVIE_RELATION_ITEM_READER = "actorMovieRelationItemReader";
    private static final String IMPORT_ACTORS_MOVIES_RELATION_JOB = "importActorsMoviesRelationJob";
    private static final String IMPORT_ACTORS_MOVIES_RELATION_STEP = "importActorsMoviesRelationStep";
    private static final ItemProcessor<? super ActorMovieRelationEntity, ? extends ActorMovieRelationEntity> FILTER_ACTOR_MOVIES_PROCESSOR = x -> "actor".equals(x.getCategory()) ? x : null;

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ResourceLoader resourceLoader;
    private final Object lock = new Object();


    @Value("${loader.chunk.actorMovieRelation}")
    private Integer chunkSize;

    @Bean
    public FlatFileItemReader<ActorMovieRelationEntity> actorMovieRelationReader(GZIPBufferedReaderFactory gZipBufferedReaderFactory) {
        FlatFileItemReader<ActorMovieRelationEntity> reader = new FlatFileItemReaderBuilder<ActorMovieRelationEntity>()
                .name(ACTOR_MOVIE_RELATION_ITEM_READER)
                .resource(resourceLoader.getResource(TITLE_PRINCIPALS_URL))
                .linesToSkip(1)
                .lineTokenizer(TOKENIZER)
                .fieldSetMapper(fieldSet -> ActorMovieRelationEntity
                        .builder()
                        .actorMovieRelationId(ActorMovieRelationId
                                .builder()
                                .actorId(Long.parseLong(fieldSet.readString(2).substring(2)))
                                .movieId(Long.parseLong(fieldSet.readString(0).substring(2)))
                                .build())
                        .category(fieldSet.readString(3))
                        .build()
                )
                .build();
        reader.setBufferedReaderFactory(gZipBufferedReaderFactory);
        return reader;
    }

    @Bean
    public ItemWriter<ActorMovieRelationEntity> actorMovieRelationWriter(ActorMovieRelationRepository actorMovieRelationRepository) {
        return items -> {
            synchronized (lock) {
                actorMovieRelationRepository.saveAll(items);
            }
        };
    }

    @Bean
    public Job importActorsMoviesRelationJob(Step importActorsMoviesRelationStep) {
        return jobBuilderFactory.get(IMPORT_ACTORS_MOVIES_RELATION_JOB)
                .incrementer(parameters -> parameters)
                .flow(importActorsMoviesRelationStep)
                .end()
                .build();
    }

    @Bean
    public Step importActorsMoviesRelationStep(ItemWriter<ActorMovieRelationEntity> writer, ItemReader<ActorMovieRelationEntity> reader, TaskExecutor stepTaskExecutor) {
        return stepBuilderFactory
                .get(IMPORT_ACTORS_MOVIES_RELATION_STEP)
                .<ActorMovieRelationEntity, ActorMovieRelationEntity>chunk(chunkSize)
                .reader(reader)
                .writer(writer)
                .processor(FILTER_ACTOR_MOVIES_PROCESSOR)
                .taskExecutor(stepTaskExecutor)
                .listener(LoggingChunkListener.builder().stepName(IMPORT_ACTORS_MOVIES_RELATION_STEP).build())
                .build();
    }
}
