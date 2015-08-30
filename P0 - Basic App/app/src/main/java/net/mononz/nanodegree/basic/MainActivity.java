package net.mononz.nanodegree.basic;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
            showToast(getString(R.string.settings));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void btn_click(View v) {
        Button button = (Button) v;
        showToast(button.getText().toString());
        switch (v.getId()) {
            case R.id.btn_movies:
                // launch movies activity
                break;
            case R.id.btn_scores:
                // launch scores activity
                break;
            case R.id.btn_library:
                // launch library activity
                break;
            case R.id.btn_bigger:
                // launch bigger activity
                break;
            case R.id.btn_bacon:
                // launch bacon activity
                break;
            case R.id.btn_capstone:
                // launch capstone activity
                break;
        }
    }

    public void showToast(String message) {
        Toast.makeText(this, String.format(getString(R.string.toast), message), Toast.LENGTH_SHORT).show();
    }

}
