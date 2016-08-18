package at.mikemitterer.mobile.chromecastsample.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import at.mikemitterer.mobile.chromecastsample.R;
import at.mikemitterer.mobile.chromecastsample.ui.adapter.FileListAdapter;
import at.mikemitterer.mobile.chromecastsample.utils.AppDirs;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    ViewHolder viewHolder;
    final List<String> files = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewHolder = new ViewHolder(this);

        updateFiles();
        viewHolder.setAdapter(new FileListAdapter(this,files));
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewHolder.cleanup();
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
        for(final File file : filesInFolder) {
            files.add(file.getName());
        }
    }
}
