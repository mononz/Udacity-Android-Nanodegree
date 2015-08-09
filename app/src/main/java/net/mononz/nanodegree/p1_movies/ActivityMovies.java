package net.mononz.nanodegree.p1_movies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import net.mononz.nanodegree.R;

public class ActivityMovies extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentMain()).commit();
    }

}