package net.mononz.builditbigger;

import android.os.AsyncTask;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import net.mononz.builditbigger.backend.myApi.MyApi;

import java.io.IOException;

public class JokeGrabber {

    JokeListener jokeListener;

    public JokeGrabber(JokeListener jokeListener) {
        this.jokeListener = jokeListener;
    }

    public void grabTastyJoke() {
        new EndpointsAsyncTask().execute();
    }

    private class EndpointsAsyncTask extends AsyncTask<Void, Void, String> {

        private MyApi myApiService = null;

        @Override
        protected String doInBackground(Void... params) {
            if(myApiService == null) {
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://sonorous-key-745.appspot.com/_ah/api/");
                myApiService = builder.build();
            }

            try {
                return myApiService.tastyJoker().execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            jokeListener.onComplete(result);
        }

    }

}
