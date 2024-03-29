package imdb.loader;

import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ChunkListener;
import org.springframework.batch.core.scope.context.ChunkContext;

@Value
@Builder
@Slf4j
public class LoggingChunkListener implements ChunkListener {

    private final String stepName;

    @Override
    public void beforeChunk(ChunkContext chunkContext) {
    }

    @Override
    public void afterChunk(ChunkContext chunkContext) {
        int written = chunkContext.getStepContext().getStepExecution().getWriteCount();
        log.info("{} written: {}", stepName, written);
    }

    @Override
    public void afterChunkError(ChunkContext chunkContext) {

    }
}
