package imdb.loader;

import lombok.RequiredArgsConstructor;
import org.apache.http.client.entity.GZIPInputStreamFactory;
import org.springframework.batch.item.file.BufferedReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Component
public class GZIPBufferedReaderFactory implements BufferedReaderFactory {

    @Value("${loader.bufferSize}")
    private Integer bufferSize;

    private final GZIPInputStreamFactory gzipInputStreamFactory;

    @Override
    public BufferedReader create(Resource resource, String encoding) throws IOException {
        return new BufferedReader(new InputStreamReader(gzipInputStreamFactory.create(resource.getInputStream()), encoding), bufferSize);
    }
}