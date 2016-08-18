package at.mikemitterer.mobile.chromecastsample.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import at.mikemitterer.mobile.chromecastsample.R;
import at.mikemitterer.mobile.chromecastsample.utils.AppDirs;
import rx.Single;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.internal.zzs.TAG;

/**
 * Zeigt die Images an
 *
 * Weitere Infos:
 *      https://github.com/codepath/android_guides/wiki/Using-an-ArrayAdapter-with-ListView
 *
 * Created by mikemitterer on 18.08.16.
 */
public class FileListAdapter extends ArrayAdapter<String> {

    private static final int BITMAP_WIDTH = 200;

    private static final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(30);

    /** Damit kann "unsubscribe" automatisiert werden. */
    private final  List<Subscription> subscriptions = new ArrayList<>();

    public FileListAdapter(final Context context, final List<String> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(final int position, View rootView, final ViewGroup parent) {

        final String filename = getItem(position);

        ViewHolder viewHolder;
        if (rootView == null) {
            rootView = LayoutInflater.from(getContext()).inflate(R.layout.listview_item_image,parent,false);

            viewHolder = new ViewHolder(rootView);
            rootView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rootView.getTag();
        }

        viewHolder.setFilename(filename);

        final ViewHolder vh = viewHolder;
        subscriptions.add(
                Single.defer(new Func0<Single<Bitmap>>() {
                    @Override
                    public Single<Bitmap> call() {
                        return Single.just(getBitmap(filename));
                    }
                })
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Bitmap>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(final Throwable e) {

                    }

                    @Override
                    public void onNext(final Bitmap bitmap) {
                        vh.setImage(getBitmap(filename));
                    }
                })
        );

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.i(TAG, "Clicked on: " + filename);
            }
        });

        return rootView;
    }

    public void cleanup() {
        unbindSignals();
    }

    // - default -------------------------------------------------------------------------------------------------------

    // - protected -----------------------------------------------------------------------------------------------------

    // - private -------------------------------------------------------------------------------------------------------

    private static final class ViewHolder {
        private final ImageView imageView;
        private final TextView filename;

        ViewHolder(final View parent) {
            imageView = (ImageView) parent.findViewById(R.id.imageView);
            filename = (TextView) parent.findViewById(R.id.tvFilename);
        }

        void setFilename(final String name) {
            filename.setText(name);
        }

        void setImage(final Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }

    private Bitmap getBitmap(final String filename) {
        Bitmap bitmap = lruCache.get(filename);
        if(bitmap == null) {
            final File file = new File(AppDirs.getImagesFolder(getContext()),filename);
            final Bitmap large = BitmapFactory.decodeFile(file.getAbsolutePath());
            bitmap = scaleToFitWidth(large,BITMAP_WIDTH);
            lruCache.put(filename,bitmap);

            Log.i(TAG, filename + " came from disk");

        } else {
            Log.d(TAG, filename + " came from Cache");
        }
        return bitmap;
    }

    private Bitmap scaleToFitWidth(Bitmap b, int width) {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
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
}
