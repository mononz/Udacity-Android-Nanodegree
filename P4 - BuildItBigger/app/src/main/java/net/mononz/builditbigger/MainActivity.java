package net.mononz.builditbigger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import net.mononz.joker.JokeActivity;
import net.mononz.joker.lib.JokeSource;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void tellJoke(View view) {
        Intent intent = new Intent(this, JokeActivity.class);
        JokeSource jokeSource = new JokeSource();
        String joke = jokeSource.getJoke();
        intent.putExtra(JokeActivity.JOKE_KEY, joke);
        startActivity(intent);
    }

}
