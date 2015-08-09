package net.mononz.nanodegree;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.stetho.Stetho;

import net.mononz.nanodegree.p1_movies.ActivityMovies;
import net.mononz.nanodegree.p1_movies.api.Api;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());

        Cursor c = getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI,
                new String[]{MoviesContract.MovieEntry._ID},
                null, null, null);
        if (c.getCount() == 0) {
            Api api = new Api(this);
            api.execute("popularity.desc");
        }
    }

    public void btn_click(View v) {
        Button button = (Button) v;
        switch (v.getId()) {
            case R.id.btn_movies:
                Intent movies = new Intent(this, ActivityMovies.class);
                startActivity(movies);
                break;
            case R.id.btn_scores:
                // launch scores activity
                showToast(button.getText().toString());
                break;
            case R.id.btn_library:
                // launch library activity
                showToast(button.getText().toString());
                break;
            case R.id.btn_bigger:
                // launch bigger activity
                showToast(button.getText().toString());
                break;
            case R.id.btn_bacon:
                // launch bacon activity
                showToast(button.getText().toString());
                break;
            case R.id.btn_capstone:
                // launch capstone activity
                showToast(button.getText().toString());
                break;
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, String.format(getString(R.string.toast), message), Toast.LENGTH_SHORT).show();
    }

}
