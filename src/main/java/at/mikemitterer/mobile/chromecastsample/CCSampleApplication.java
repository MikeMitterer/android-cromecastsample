package at.mikemitterer.mobile.chromecastsample;

import android.app.Application;
import android.util.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static at.mikemitterer.mobile.chromecastsample.utils.AppDirs.getImagesFolder;

/**
 * Kopiert die Files in assets in den Datenbereich der App
 *
 * Created by mikemitterer on 18.08.16.
 */

public class CCSampleApplication extends Application {
    private static final String TAG = "CCSampleApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        final File imagesFolder = new File(getImagesFolder(this));
        if(!imagesFolder.exists()) {
            //noinspection ResultOfMethodCallIgnored
            imagesFolder.mkdirs();
        }
        try {
            final String[] assets = getAssets().list("");
            if(assets != null) {
                final List<String> files = Arrays.asList(assets);
                for(final String filename : files) {
                    if(filename.endsWith(".jpg")) {
                        final File target = new File(imagesFolder, filename);
                        FileUtils.copyInputStreamToFile(getAssets().open(filename), target);

                        Log.d(TAG, filename + " copied to " + imagesFolder.getAbsolutePath());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // - default -------------------------------------------------------------------------------------------------------

    // - protected -----------------------------------------------------------------------------------------------------

    // - private -------------------------------------------------------------------------------------------------------

}
