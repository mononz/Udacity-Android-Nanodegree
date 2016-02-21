package net.mononz.builditbigger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import net.mononz.joker.JokeActivity;

public class MainActivity extends AppCompatActivity {

    private InterstitialAd mInterstitialAd;
    private ProgressBar spinner;

    Intent intent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = (ProgressBar) findViewById(R.id.progress_wait);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_ad_unit_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
        requestNewInterstitial();
    }

    public void tellJoke(View view) {
        spinner.setVisibility(View.VISIBLE);
        new JokeGrabber(new JokeListener() {
            @Override
            public void onComplete(String joke) {
                spinner.setVisibility(View.GONE);
                intent = new Intent(MainActivity.this, JokeActivity.class);
                intent.putExtra(JokeActivity.JOKE_KEY, joke);
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }
        }).grabTastyJoke();
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.test_device))
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

}