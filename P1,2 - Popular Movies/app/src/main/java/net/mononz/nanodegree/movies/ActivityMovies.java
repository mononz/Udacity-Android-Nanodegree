package net.mononz.nanodegree.movies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.stetho.Stetho;

import net.mononz.nanodegree.movies.data.MoviesContract;
import net.mononz.nanodegree.movies.sync.MovieSyncAdapter;

public class ActivityMovies extends AppCompatActivity {

    public Toolbar toolbar;

    private static final long THRESHOLD_HOURS = 6;
    private static final long THRESHOLD_MILLIS = THRESHOLD_HOURS * 60 * 60 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentMain()).commit();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());

        syncData();

    }

    private void syncData() {

        MovieSyncAdapter.initializeSyncAdapter(this);

        Cursor c = getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI,
                new String[]{MoviesContract.MovieEntry._ID},
                null, null, null);

        Preferences_Manager preferences_manager = new Preferences_Manager(this);
        long lastSync = preferences_manager.getLastSync();
        long nextSync = System.currentTimeMillis() - (lastSync + THRESHOLD_MILLIS);
        if (nextSync > 0 || c.getCount() == 0) {
            MovieSyncAdapter.syncImmediately(this);
        } else {
            Log.d("Sync wait", "" + Math.abs(nextSync) + "ms till next sync");
        }
        c.close();
    }

}