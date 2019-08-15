package imdb.loader;

import imdb.api.control.ActorRepository;
import imdb.api.entity.ActorEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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

import static imdb.loader.BatchConfiguration.NAME_BASICS_URL;
import static imdb.loader.BatchConfiguration.TOKENIZER;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Actor {

    private static final String IMPORT_ACTORS_JOB = "importActorsJob";
    private static final String ACTOR_ITEM_READER = "actorItemReader";
    private static final String IMPORT_ACTORS_STEP = "importActorsStep";

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final ResourceLoader resourceLoader;

    @Value("${loader.chunk.actor}")
    private Integer chunkSize;

    @Bean
    public Step importActorsStep(ItemWriter<ActorEntity> writer, ItemReader<ActorEntity> reader, TaskExecutor stepTaskExecutor) {
        return stepBuilderFactory
                .get(IMPORT_ACTORS_STEP)
                .<ActorEntity, ActorEntity>chunk(chunkSize)
                .reader(reader)
                .writer(writer)
                .taskExecutor(stepTaskExecutor)
                .listener(LoggingChunkListener.builder().stepName(IMPORT_ACTORS_STEP).build())
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
    public ItemWriter<ActorEntity> actorWriter(ActorRepository actorRepository) {
        return actorRepository::saveAll;
    }

    @Bean
    public FlatFileItemReader<ActorEntity> actorReader(GZIPBufferedReaderFactory gZipBufferedReaderFactory) {
        FlatFileItemReader<ActorEntity> reader = new FlatFileItemReaderBuilder<ActorEntity>()
                .name(ACTOR_ITEM_READER)
                .resource(resourceLoader.getResource(NAME_BASICS_URL))
                .linesToSkip(1)
                .lineTokenizer(TOKENIZER)
                .fieldSetMapper(fieldSet -> ActorEntity
                        .builder()
                        .id(Long.parseLong(fieldSet.readString(0).substring(2)))
                        .name(fieldSet.readString(1))
                        .build()
                )
                .build();
        reader.setBufferedReaderFactory(gZipBufferedReaderFactory);
        return reader;
    }
}
