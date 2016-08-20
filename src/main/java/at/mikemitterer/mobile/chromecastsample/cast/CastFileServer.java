package at.mikemitterer.mobile.chromecastsample.cast;

import java.io.IOException;

/**
 * Basis für den Fileserver auf Android
 *
 * Created by mikemitterer on 20.08.16.
 */
public interface CastFileServer {
    void start() throws IOException;
    void stop();

    String getUrl();
    String getMimeType(final String filename);
}
