package net.mononz.joker;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import net.mononz.builditbigger.backend.myApi.MyApi;

import java.io.IOException;

public class JokeActivityFragment extends Fragment {

    private TextView jokeTextView;

    public JokeActivityFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_joke, container, false);

        Intent intent = getActivity().getIntent();
        String joke = intent.getStringExtra(JokeActivity.JOKE_KEY);
        jokeTextView = (TextView) root.findViewById(R.id.joke_textview);
        if (joke != null && joke.length() != 0) {
            jokeTextView.setText(joke);
        }

        new EndpointsAsyncTask().execute(new Pair<Context, String>(getActivity(), "Manfred"));

        return root;
    }

    private class EndpointsAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {

        private MyApi myApiService = null;
        private Context context;

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            if(myApiService == null) {
                MyApi.Builder builder = new MyApi.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://sonorous-key-745.appspot.com/_ah/api/");
                myApiService = builder.build();
            }

            context = params[0].first;
            String name = params[0].second;

            try {
                return myApiService.sayHi(name).execute().getData();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            jokeTextView.setText(result);
        }

    }

}