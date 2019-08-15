package imdb.loader;

import imdb.api.control.MovieRepository;
import imdb.api.entity.MovieEntity;
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

import static imdb.loader.BatchConfiguration.TITLE_BASICS_URL;
import static imdb.loader.BatchConfiguration.TOKENIZER;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Movie {

    private static final String MOVIE_ITEM_READER = "movieItemReader";
    private static final String IMPORT_MOVIES_STEP = "importMoviesStep";
    private static final ItemProcessor<? super MovieEntity, ? extends MovieEntity> FILTER_MOVIE_PROCESSOR = x -> x.isMovie() ? x : null;


    private final StepBuilderFactory stepBuilderFactory;
    private final ResourceLoader resourceLoader;

    @Value("${loader.chunk.movie}")
    private Integer chunkSize;

    @Bean
    public ItemWriter<MovieEntity> movieWriter(MovieRepository movieRepository) {
        return items -> {
            movieRepository.saveAll(items);
            movieRepository.flush();
        };
    }

    @Bean
    public FlatFileItemReader<MovieEntity> movieReader(GZIPBufferedReaderFactory gzipBufferedReaderFactory) {
        FlatFileItemReader<MovieEntity> reader = new FlatFileItemReaderBuilder<MovieEntity>()
                .name(MOVIE_ITEM_READER)
                .resource(resourceLoader.getResource(TITLE_BASICS_URL))
                .linesToSkip(1)
                .lineTokenizer(TOKENIZER)
                .fieldSetMapper(fieldSet -> MovieEntity
                        .builder()
                        .id(Long.parseLong(fieldSet.readString(0).substring(2)))
                        .title(fieldSet.readString(2))
                        .year(Optional.ofNullable(fieldSet.readString(5)).map(Integer::parseInt).orElse(null))
                        .movie(Optional.ofNullable(fieldSet.readString(1)).filter(x -> x.contains("movie")).isPresent())
                        .build()
                )
                .build();
        reader.setBufferedReaderFactory(gzipBufferedReaderFactory);
        return reader;
    }

    @Bean
    public Step importMoviesStep(ItemWriter<MovieEntity> writer, ItemReader<MovieEntity> reader, TaskExecutor stepTaskExecutor) {
        return stepBuilderFactory
                .get(IMPORT_MOVIES_STEP)
                .<MovieEntity, MovieEntity>chunk(chunkSize)
                .reader(reader)
                .writer(writer)
                .processor(FILTER_MOVIE_PROCESSOR)
                .taskExecutor(stepTaskExecutor)
                .listener(LoggingChunkListener.builder().stepName(IMPORT_MOVIES_STEP).build())
                .build();
    }
}
