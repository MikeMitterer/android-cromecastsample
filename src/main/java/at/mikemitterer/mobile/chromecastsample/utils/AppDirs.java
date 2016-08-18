package at.mikemitterer.mobile.chromecastsample.utils;

import android.content.Context;
import android.os.Environment;
import org.apache.commons.lang3.Validate;

import java.io.File;

/**
 * Created by mikemitterer on 18.08.16.
 */

public class AppDirs {
    private static final String CONFIG_FOLDER = "config";
    private static final String DATA_FOLDER   = "data";
    private static final String IMAGES_FOLDER = "images";

    /**
     * Images der Applikation
     */
    public static  String getImagesFolder(final Context context) {
        Validate.notNull(context);
        return getDataFolder(context) + File.separator + IMAGES_FOLDER;
    }

    // - default -------------------------------------------------------------------------------------------------------

    // - protected -----------------------------------------------------------------------------------------------------

    // - private -------------------------------------------------------------------------------------------------------

    /**
     * Basis-Verzeichnis.
     * In der Regel so etwas wie /data/data/\<package\>
     */
    private static String getDataFolder(final Context context) {
        return Environment.getDataDirectory() + File.separator + DATA_FOLDER +
                File.separator + context.getPackageName();
    }

}

