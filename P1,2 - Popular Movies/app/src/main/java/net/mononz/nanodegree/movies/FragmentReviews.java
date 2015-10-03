package net.mononz.nanodegree.movies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.mononz.nanodegree.movies.api.reviews.Review;
import net.mononz.nanodegree.movies.api.reviews.Reviews;
import net.mononz.nanodegree.movies.api.videos.Video;
import net.mononz.nanodegree.movies.sync.Network;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

public class FragmentReviews extends Fragment {

    @InjectView(R.id.reviews) protected RecyclerView recList;

    private static final String LOG_TAG = FragmentReviews.class.getSimpleName();

    private int mMovieId;
    private String TAG_MOVIE_ID = "mMovieId";

    private ArrayList<Review> reviewArrayList = new ArrayList<>();

    public static FragmentReviews newInstance(int movie_id) {
        FragmentReviews fragment = new FragmentReviews();
        Bundle args = new Bundle();
        fragment.mMovieId = movie_id;
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentReviews() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reviews, container, false);
        ButterKnife.inject(this, rootView);

        if (savedInstanceState != null) {
            mMovieId = savedInstanceState.getInt(TAG_MOVIE_ID);
        }

        Adapter_Video adapter = new Adapter_Video();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setHasFixedSize(true);
        recList.setLayoutManager(llm);
        recList.setAdapter(adapter);
        getAllReviews();

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAG_MOVIE_ID, mMovieId);
    }

    private void getAllReviews() {
        Call<Reviews> reviewsCall = new Network().service.getMovieReviews(mMovieId, getString(R.string.tmdb_api_key));
        reviewsCall.enqueue(new Callback<Reviews>() {
            @Override
            public void onResponse(Response<Reviews> response) {
                Log.d("onResponse", "" + response.code() + " - " + response.message());
                if (response.isSuccess()) {
                    if (response.body().results.size() == 0) {
                        Toast.makeText(getActivity(), "No Reviews", Toast.LENGTH_LONG).show();
                    } else {
                        reviewArrayList = response.body().results;
                        recList.getAdapter().notifyDataSetChanged();
                    }
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    public class Adapter_Video extends RecyclerView.Adapter<Adapter_Video.ShowViewHolder> {

        public Adapter_Video() { }

        @Override
        public int getItemCount() {
            return reviewArrayList.size();
        }

        @Override
        public void onBindViewHolder(final ShowViewHolder view_holder, final int position) {
            final Review obj = reviewArrayList.get(position);
            view_holder.vAuthor.setText(obj.author);
            view_holder.vContent.setText(Html.fromHtml(obj.content));
            view_holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(obj.url)));
                }
            });
        }

        @Override
        public ShowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_review, viewGroup, false);
            return new ShowViewHolder(itemView);
        }

        public class ShowViewHolder extends RecyclerView.ViewHolder {

            protected View mView;
            @InjectView(R.id.author) protected TextView vAuthor;
            @InjectView(R.id.content) protected TextView vContent;

            public ShowViewHolder(View v) {
                super(v);
                mView = v;
                ButterKnife.inject(this, v);
            }
        }

    }
}