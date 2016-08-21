package at.mikemitterer.mobile.chromecastsample.cast;

import android.util.Log;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;
import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Fileserver für die MEDIA-Files
 *
 * Weitere Infos:
 *      http://www.java2s.com/Open-Source/Android_Free_Code/Video/Image/com_google_plus_dougnlamb_firecastFireCastService_java.htm
 *      https://github.com/dougnlamb/FireCast
 *      https://github.com/omerjerk/RemoteDroid
 *
 * Created by mikemitterer on 19.08.16.
 */

public class CastAsyncFileServer implements CastFileServer {
    private final static String TAG = "CastFileServer";

    public final static int PORT = 8900;

    public static final  String MIME_TYPE_JPG   = "image/jpeg";
    public static final  String MIME_TYPE_PNG   = "image/png";
    public static final  String MIME_TYPE_MP4   = "videos/mp4";

    private static final int    JPEG_COMPRESSION = 60;

    private final String url;
    private final String docRoot;

    private AsyncHttpServer server       = new AsyncHttpServer();
    //private AsyncServer     mAsyncServer = new AsyncServer();


    public CastAsyncFileServer(final String url, final String docRoot) {
        this.url = url;
        this.docRoot = docRoot;
    }

    @Override
    public void start(){

        server.get("/.*", new HttpServerRequestCallback() {
            @Override
            public void onRequest(final AsyncHttpServerRequest request, final AsyncHttpServerResponse response) {
                final String path = request.getPath();

                final File file = new File(docRoot,path);
                if(file.exists()) {
                    //response.setContentType(getMimeType(path));
                    response.sendFile(file);
                    //try {
                    //    response.sendStream(new FileInputStream(file),file.length());
                    //} catch (FileNotFoundException e) {
                    //    e.printStackTrace();
                    //}
                }
            }
        });

        server.listen(AsyncServer.getDefault(),PORT);
        Log.d(TAG,String.format("Mediaserver started on port: %d",PORT));
    }

    @Override
    public void stop() {
        server.stop();
        Log.d(TAG,String.format("Mediaserver stopped!",PORT));
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getMimeType(final String filename) {
        final String extension = FilenameUtils.getExtension(filename).toLowerCase();

        if (extension.equals("jpg") || extension.equals("jpeg")) {
            return MIME_TYPE_JPG;

        } else if(extension.equals("png")) {
            return MIME_TYPE_PNG;

        } else if(extension.equals("mp4")) {
            return MIME_TYPE_MP4;
        }
        throw new IllegalArgumentException("Wrong filename: " + filename + ". Only JPG and PNG is supported");
    }


    // - default -------------------------------------------------------------------------------------------------------

    // - protected -----------------------------------------------------------------------------------------------------

    // - private -------------------------------------------------------------------------------------------------------

    /**
     * Schneidet optimiert den FilePfad und schneidet alls nach einem eventuellen ? weg
     */
    private String normalizeFileUri(final String uri) {
        String normalized = uri.trim().replace(File.separatorChar, '/');
        if (normalized.indexOf('?') >= 0) {
            normalized = normalized.substring(0, normalized.indexOf('?'));
        }
        return normalized;
    }



}
