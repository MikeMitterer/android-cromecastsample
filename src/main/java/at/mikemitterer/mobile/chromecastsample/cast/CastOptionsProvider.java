package at.mikemitterer.mobile.chromecastsample.cast;

import android.content.Context;
import at.mikemitterer.mobile.chromecastsample.R;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;

import java.util.List;

/**
 * Registrierung der ID:
 *      https://developers.google.com/cast/docs/registration#RegisterDevice
 *      https://cast.google.com/publish/#/overview
 *
 * Serialnumber - Chromecast V1:
 *      https://photos.google.com/photo/AF1QipOlFnFVQI7-hZKhzbjMceujAVaCYeYXzy27gWYJ?hl=de
 *
 * Created by mikemitterer on 19.08.16.
 */
public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
        return new CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.app_id))
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}