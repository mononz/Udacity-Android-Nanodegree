package net.mononz.builditbigger;

import android.test.AndroidTestCase;
import android.test.UiThreadTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class JokeGrabberTest extends AndroidTestCase implements JokeListener {

    private JokeGrabber mJoker;
    private CountDownLatch signal;
    private String joke = "";

    protected void setUp() throws Exception {
        super.setUp();
        signal = new CountDownLatch(1);
        mJoker = new JokeGrabber(this);
    }

    @UiThreadTest
    public void testDownload() throws InterruptedException {
        mJoker.grabTastyJoke();
        signal.await(30, TimeUnit.SECONDS);
        assertTrue("Valid Joke!", joke != null);
    }

    @Override
    public void onComplete(String joke) {
        this.joke = joke;
        signal.countDown();
    }


}
