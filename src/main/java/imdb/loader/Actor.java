package imdb.loader;

import imdb.api.control.ActorRepository;
import imdb.api.entity.ActorEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
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

import java.util.Optional;

import static imdb.loader.BatchConfiguration.NAME_BASICS_URL;
import static imdb.loader.BatchConfiguration.TOKENIZER;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Actor {

    private static final String ACTOR_ITEM_READER = "actorItemReader";
    private static final String IMPORT_ACTORS_STEP = "importActorsStep";
    private static final ItemProcessor<? super ActorEntity, ? extends ActorEntity> FILTER_ACTOR_PROCESSOR = x -> x.isActor() ? x : null;


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
                .processor(FILTER_ACTOR_PROCESSOR)
                .taskExecutor(stepTaskExecutor)
                .listener(LoggingChunkListener.builder().stepName(IMPORT_ACTORS_STEP).build())
                .build();
    }

    @Bean
    public ItemWriter<ActorEntity> actorWriter(ActorRepository actorRepository) {
        return items -> {
            actorRepository.saveAll(items);
            actorRepository.flush();
        };
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
                        .actor(Optional.ofNullable(fieldSet.readString(4)).filter(x -> x.contains("actor") || x.contains("actress")).isPresent())
                        .build()
                )
                .build();
        reader.setBufferedReaderFactory(gZipBufferedReaderFactory);
        return reader;
    }
}
