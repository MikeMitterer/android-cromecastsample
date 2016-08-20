package at.mikemitterer.mobile.chromecastsample.ui;

import android.app.Activity;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import at.mikemitterer.mobile.chromecastsample.R;
import at.mikemitterer.mobile.chromecastsample.cast.CastAsyncFileServer;
import at.mikemitterer.mobile.chromecastsample.cast.CastFileServer;
import at.mikemitterer.mobile.chromecastsample.eventbus.RxEventBus;
import at.mikemitterer.mobile.chromecastsample.eventbus.RxEventBusProvider;
import at.mikemitterer.mobile.chromecastsample.eventbus.events.AbstractDataEvent;
import at.mikemitterer.mobile.chromecastsample.eventbus.events.MessageEvent;
import at.mikemitterer.mobile.chromecastsample.ui.adapter.FileListAdapter;
import at.mikemitterer.mobile.chromecastsample.utils.AppDirs;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.images.WebImage;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sample
 *      https://github.com/hyongbai/simpleChromeCast
 *
 * Mime-Types:
 *      https://android.googlesource.com/platform/external/nanohttpd/+/lollipop-mr1-cts-release/webserver/src/main/java/fi/iki/elonen/SimpleWebServer.java
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Toolbar  mToolbar;

    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;

    private CastSession                         mCastSession;
    private SessionManagerListener<CastSession> mSessionManagerListener;

    private CastFileServer mediaServer;

    private RxEventBus<AbstractDataEvent> eventBus = RxEventBusProvider.get();

    ViewHolder viewHolder;
    final List<String> files = new ArrayList<>();

    /** Damit kann "unsubscribe" automatisiert werden. */
    final List<Subscription> subscriptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setupActionBar();

        viewHolder = new ViewHolder(this);

        updateFiles();
        viewHolder.setAdapter(new FileListAdapter(this, files));

        setupCastListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
        mCastSession = mCastContext.getSessionManager().getCurrentCastSession();
    }

    private void startFileServer() {
        final WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        final int ipAddress = wifiInfo.getIpAddress();

        final String url = String.format("http://%d.%d.%d.%d:%d",
                (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff),
                CastAsyncFileServer.PORT);

        final String docRoot = AppDirs.getImagesFolder(this);
        mediaServer = new CastAsyncFileServer(url,docRoot);

        try {
            mediaServer.start();
            Log.i(TAG, "Server MEDIA-Files on: " + url + ", DocRoot: " + docRoot);

        } catch (IOException ioe) {
            Log.e(TAG, "The server could not start.");
            ioe.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);

        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);

        startFileServer();
        bindSignals();
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewHolder.cleanup();
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);

        if(mediaServer != null) {
            mediaServer.stop();
        }
        unbindSignals();
    }



// - default -------------------------------------------------------------------------------------------------------

    // - protected -----------------------------------------------------------------------------------------------------

    // - private -------------------------------------------------------------------------------------------------------

    private static final class ViewHolder {
        private final ListView listView;

        ViewHolder(final Activity activity) {
            this.listView = (ListView) activity.findViewById(R.id.listView);
        }

        void setAdapter(final ArrayAdapter<String> adapter) {
            listView.setAdapter(adapter);
        }

        void update() {
            //noinspection unchecked
            ((ArrayAdapter<String>) listView.getAdapter()).notifyDataSetChanged();
        }

        void cleanup() {
            final FileListAdapter adapter = (FileListAdapter) listView.getAdapter();
            adapter.cleanup();
        }
    }

    private void updateFiles() {
        files.clear();

        final File imagesFolder = new File(AppDirs.getImagesFolder(this));
        final Collection<File> filesInFolder = FileUtils.listFiles(imagesFolder, null, false);
        for (final File file : filesInFolder) {
            files.add(file.getName());
        }
    }

    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                Log.d(TAG,"Cast - onSessionEnded, Error: " + error);
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                Log.d(TAG,"Cast - onSessionResumed, wasSuspended: " + wasSuspended);
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                Log.d(TAG,"Cast - onSessionEnded, Error: " + error);
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                Log.d(TAG,"Cast - onSessionStarted");
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                Log.d(TAG,"Cast - onSessionStartFailed, Error: " + error);
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {
                Log.d(TAG,"Cast - onSessionStarting");
            }

            @Override
            public void onSessionEnding(CastSession session) {
                Log.d(TAG,"Cast - onSessionEnding");
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
                Log.d(TAG,"Cast - onSessionResuming");
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
                Log.d(TAG,"Cast - onSessionSuspended, Reason: " + reason);
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;
                invalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                invalidateOptionsMenu();
            }
        };
    }

    private void bindSignals() {
        subscriptions.add(
            eventBus.observe()
                .subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                .subscribe(new Subscriber<AbstractDataEvent>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(final Throwable e) {

                    }

                    @Override
                    public void onNext(final AbstractDataEvent abstractDataEvent) {
                        if(abstractDataEvent instanceof MessageEvent) {
                            final MessageEvent event = (MessageEvent) abstractDataEvent;

                            Log.d(TAG,"Show: " + event.getData());
                            loadRemoteMedia(event.getData());
                        }
                    }
                })
        );
    }

    /** Signale / Events werden wieder entfernt */
    private void unbindSignals() {
        for(final Subscription subscription : subscriptions) {
            if(!subscription.isUnsubscribed()) {
                subscription.unsubscribe();
            }
        }
        subscriptions.clear();
    }

    private void loadRemoteMedia(final String filename) {
        if (mCastSession == null) {
            return;
        }
        RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }

        if(mediaServer == null) {
            return;
        }

        final boolean autoPlay = true;

        remoteMediaClient.load(buildMediaInfo(filename), autoPlay)
            .setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {
                @Override
                public void onResult(@NonNull final RemoteMediaClient.MediaChannelResult result) {
                    if(result.getStatus().isSuccess()) {
                        Log.i(TAG,"Media loaded successfully!");
                    } else {
                        Log.e(TAG,result.getStatus().toString());
                    }

                }
            });
    }

    private MediaInfo buildMediaInfo(final String filename) {
        MediaMetadata movieMetadata = new MediaMetadata(getMediaType(filename));

        movieMetadata.putString(MediaMetadata.KEY_TITLE, filename);
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, filename);

        final List<String> images = new ArrayList<>();
        images.add("https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/480x270/BigBuckBunny.jpg");
        images.add("https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/images/780x1200/BigBuckBunny-780x1200.jpg");

        String url = mediaServer.getUrl() + "/" + filename;
        final String contentType = mediaServer.getMimeType(filename);

        movieMetadata.addImage(new WebImage(Uri.parse(images.get(0))));
        //movieMetadata.addImage(new WebImage(Uri.parse(images.get(1))));
        //movieMetadata.addImage(new WebImage(Uri.parse(url)));

        //url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/mp4/BigBuckBunny.mp4";
        return new MediaInfo.Builder(url)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(contentType)
                .setMetadata(movieMetadata)
                .setStreamDuration(0)
                .build();
    }

    private int getMediaType(final String filename) {
        final String extension = FilenameUtils.getExtension(filename).toLowerCase();

        if(extension.equals("mp4")) {
            return MediaMetadata.MEDIA_TYPE_MOVIE;
        }

        if(extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
            return MediaMetadata.MEDIA_TYPE_PHOTO;

        }

        return MediaMetadata.MEDIA_TYPE_GENERIC;
    }

}
