package net.mononz.nanodegree;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.settings) {
            Toast.makeText(this, getString(R.string.toast_no_settings), Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btn_spotify(View v) {
        Toast.makeText(this, getString(R.string.toast_spotify), Toast.LENGTH_SHORT).show();
    }

    public void btn_scores(View v) {
        Toast.makeText(this, getString(R.string.toast_scores), Toast.LENGTH_SHORT).show();
    }

    public void btn_library(View v) {
        Toast.makeText(this, getString(R.string.toast_library), Toast.LENGTH_SHORT).show();
    }

    public void btn_bigger(View v) {
        Toast.makeText(this, getString(R.string.toast_bigger), Toast.LENGTH_SHORT).show();
    }

    public void btn_bacon(View v) {
        Toast.makeText(this, getString(R.string.toast_bacon), Toast.LENGTH_SHORT).show();
    }

    public void btn_capstone(View v) {
        Toast.makeText(this, getString(R.string.toast_capstone), Toast.LENGTH_SHORT).show();
    }

}
