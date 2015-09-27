package net.mononz.nanodegree.movies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.stetho.Stetho;

import net.mononz.nanodegree.movies.data.MoviesContract;
import net.mononz.nanodegree.movies.sync.MovieSyncAdapter;

public class ActivityMovies extends AppCompatActivity implements FragmentMain.Callbacks {

    private boolean mTwoPane;
    private static final long THRESHOLD_HOURS = 6;
    private static final long THRESHOLD_MILLIS = THRESHOLD_HOURS * 60 * 60 * 1000;

    public Toolbar main_toolbar;
    public Toolbar detail_toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new FragmentMain())
                .commit();

        main_toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        if (findViewById(R.id.detail_container) != null) {
            mTwoPane = true;
            detail_toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
            setSupportActionBar(detail_toolbar);
            main_toolbar.setTitle(getString(R.string.app_name));
        } else {
            setSupportActionBar(main_toolbar);
        }

        if (BuildConfig.DEBUG) {
            Stetho.initialize(Stetho.newInitializerBuilder(this)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                    .build());
        }

        syncData();
    }

    @Override
    public void onItemSelected(int id) {
        FragmentDetail fragmentDetail = FragmentDetail.newInstance(id);
        if (mTwoPane) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragmentDetail)
                    .commit();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_container, fragmentDetail)
                    .addToBackStack("detail")
                    .commit();
        }
    }
    @Override
    public void onUpdateToolbar(String str) {
        main_toolbar.setSubtitle(str);
    }

    public void toolbars(String str) {
        if (mTwoPane) {
            detail_toolbar.setTitle(str);
        } else {
            main_toolbar.setTitle(str);
        }
    }

    private void syncData() {
        MovieSyncAdapter.initializeSyncAdapter(this);

        Cursor c = getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI,
                new String[]{MoviesContract.MovieEntry.FULL_ID},
                null, null, null);
        if (c != null) {
            Preferences_Manager preferences_manager = new Preferences_Manager(this);
            long lastSync = preferences_manager.getLastSync();
            long nextSync = System.currentTimeMillis() - (lastSync + THRESHOLD_MILLIS);
            if (nextSync > 0 || c.getCount() == 0) {
                Log.d("Sync now!", "" + nextSync + "ms overdue");
                MovieSyncAdapter.syncImmediately(this);
            } else {
                Log.d("Sync wait", "" + Math.abs(nextSync) + "ms till next sync");
            }
            c.close();
        }
    }

}