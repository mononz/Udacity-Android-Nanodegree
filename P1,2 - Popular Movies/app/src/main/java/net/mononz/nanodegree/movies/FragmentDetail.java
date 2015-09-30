package net.mononz.nanodegree.movies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import net.mononz.nanodegree.movies.api.reviews.Reviews;
import net.mononz.nanodegree.movies.api.videos.Video;
import net.mononz.nanodegree.movies.api.videos.Videos;
import net.mononz.nanodegree.movies.data.FavouritesContract;
import net.mononz.nanodegree.movies.data.MoviesContract;
import net.mononz.nanodegree.movies.sync.Network;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

public class FragmentDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    @InjectView(R.id.poster) protected ImageView mPoster;
    @InjectView(R.id.popularity) protected TextView mPopularity;
    @InjectView(R.id.rating) protected TextView mRating;
    @InjectView(R.id.released) protected TextView mReleased;
    @InjectView(R.id.plot) protected TextView mPlot;
    @InjectView(R.id.card_video) protected CardView mCardVideo;
    @InjectView(R.id.video) protected RecyclerView recList;
    @InjectView(R.id.card_review) protected CardView mCardReview;
    @InjectView(R.id.review) protected TextView mReview;
    @InjectView(R.id.more_reviews) protected TextView mMoreReviews;

    private static final String LOG_TAG = FragmentDetail.class.getSimpleName();

    private Cursor mDetailCursor;
    private static final int CURSOR_LOADER_ID = 0;

    private int mMovieId;
    private String TAG_MOVIE_ID = "mMovieId";

    private boolean favourite;
    private ArrayList<Video> videoArrayList = new ArrayList<>();

    private Menu menu;

    public static FragmentDetail newInstance(int movie_id) {
        FragmentDetail fragment = new FragmentDetail();
        Bundle args = new Bundle();
        fragment.mMovieId = movie_id;
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentDetail() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.inject(this, rootView);

        if (savedInstanceState != null) {
            // Restore last state
            mMovieId = savedInstanceState.getInt(TAG_MOVIE_ID);
            getLoaderManager().initLoader(CURSOR_LOADER_ID, new Bundle(), FragmentDetail.this);
        } else {
            Bundle args = this.getArguments();
            getLoaderManager().initLoader(CURSOR_LOADER_ID, args, FragmentDetail.this);
        }

        Adapter_Video adapter = new Adapter_Video();
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.HORIZONTAL);
        recList.setHasFixedSize(true);
        recList.setLayoutManager(llm);
        recList.setAdapter(adapter);

        mMoreReviews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Coming soon", Toast.LENGTH_SHORT).show();
            }
        });

        Bundle args = this.getArguments();
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        this.menu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
            case R.id.sharing:
                int nameIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Check out the awesome " + mDetailCursor.getString(nameIndex) + " trailer at http://www.youtube.com/watch?v=" + videoArrayList.get(0).key);
                        startActivity(Intent.createChooser(sharingIntent, "Share using"));
                break;
            case R.id.favourite_yes:
                getActivity().getContentResolver().delete(
                        FavouritesContract.FavouritesEntry.CONTENT_URI,
                        FavouritesContract.FavouritesEntry._ID + "=?",
                        new String[]{String.valueOf(mMovieId)});
                favourite = !favourite;
                showFavouriteIcon();
                break;
            case R.id.favourite_no:
                ContentValues values = new ContentValues();
                values.put(FavouritesContract.FavouritesEntry._ID, String.valueOf(mMovieId));
                values.put(FavouritesContract.FavouritesEntry.COLUMN_DATE_CREATED, System.currentTimeMillis());
                getActivity().getContentResolver().insert(FavouritesContract.FavouritesEntry.CONTENT_URI, values);
                favourite = !favourite;
                showFavouriteIcon();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(TAG_MOVIE_ID, mMovieId);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        String selection = null;
        String [] selectionArgs = null;
        if (args != null) {
            selection = MoviesContract.MovieEntry.FULL_ID;
            selectionArgs = new String[]{String.valueOf(mMovieId)};
        }
        return new CursorLoader(getActivity(),
                ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, mMovieId), null,
                selection, selectionArgs, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDetailCursor = data;
        mDetailCursor.moveToFirst();
        //DatabaseUtils.dumpCursor(data);

        int nameIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
        ((ActivityMovies) getActivity()).toolbars(mDetailCursor.getString(nameIndex));

        int posterIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_BACKDROP);
        Glide.with(this)
                .load(Network.getImage(true, mDetailCursor.getString(posterIndex)))
                .into(mPoster);

        int popularityIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POPULARITY);
        mPopularity.setText(mDetailCursor.getString(popularityIndex));

        int ratingAverageIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        int ratingCountIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT);
        String str = "" + mDetailCursor.getDouble(ratingAverageIndex) + " (" + mDetailCursor.getInt(ratingCountIndex) + ")";
        mRating.setText(str);

        int plotIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        mPlot.setText(mDetailCursor.getString(plotIndex));

        int releasedIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        mReleased.setText(mDetailCursor.getString(releasedIndex));

        int createdDateIndex = mDetailCursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_DATE_CREATED);
        favourite = !mDetailCursor.isNull(createdDateIndex);

        showFavouriteIcon();
        getVideos();
        getReviews();

    }

    // reset CursorAdapter on Loader Reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mDetailCursor = null;
    }

    private void showFavouriteIcon() {
        if (menu != null) {
            menu.findItem(R.id.favourite_yes).setVisible(favourite);
            menu.findItem(R.id.favourite_no).setVisible(!favourite);
        }
    }

    private void getVideos() {
        Call<Videos> videosCall = new Network().service.getMovieVideos(mMovieId, getString(R.string.tmdb_api_key));
        videosCall.enqueue(new Callback<Videos>() {
            @Override
            public void onResponse(Response<Videos> response) {
                Log.d("onResponse", "" + response.code() + " - " + response.message());
                if (response.isSuccess()) {
                    if (response.body().results.size() == 0) {
                        mCardVideo.setVisibility(View.GONE);
                    } else {
                        mCardVideo.setVisibility(View.VISIBLE);
                        videoArrayList = response.body().results;
                        recList.getAdapter().notifyDataSetChanged();
                        if (menu != null && videoArrayList.size()>0) {
                            menu.findItem(R.id.sharing).setVisible(true);
                        }
                    }
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    private void getReviews() {
        Call<Reviews> reviewsCall = new Network().service.getMovieReviews(mMovieId, getString(R.string.tmdb_api_key));
        reviewsCall.enqueue(new Callback<Reviews>() {
            @Override
            public void onResponse(final Response<Reviews> response) {
                Log.d("onResponse", "" + response.code() + " - " + response.message());
                if (response.isSuccess()) {
                    if (response.body().results.size() == 0) {
                        mCardReview.setVisibility(View.GONE);
                    } else {
                        mCardReview.setVisibility(View.VISIBLE);
                        String str = response.body().results.get(0).content;
                        if (response.body().results.size() > 1 || str.length()>256) {
                            mMoreReviews.setVisibility(View.VISIBLE);
                        }
                        if (str.length()>256) {
                            str = str.substring(0, 256) + "...";
                        }
                        mReview.setText(str);
                        mReview.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().results.get(0).url)));
                            }
                        });
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
            return videoArrayList.size();
        }

        @Override
        public void onBindViewHolder(final ShowViewHolder view_holder, final int position) {
            final Video obj = videoArrayList.get(position);
            view_holder.vType.setText(obj.type);
            String nme = obj.name;
            if (nme.length() > 20)
                nme = nme.substring(0, 20) + "...";
            view_holder.vName.setText(nme);
            view_holder.vLink.setText(obj.site);
            view_holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + obj.key)));
                    //mCallbacks.onItemSelected(obj.id);
                }
            });
        }

        @Override
        public ShowViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.element_video, viewGroup, false);
            return new ShowViewHolder(itemView);
        }

        public class ShowViewHolder extends RecyclerView.ViewHolder {

            protected View mView;
            protected TextView vType;
            protected TextView vName;
            protected TextView vLink;

            public ShowViewHolder(View v) {
                super(v);
                mView = v;
                vType  = (TextView) v.findViewById(R.id.type);
                vName  = (TextView) v.findViewById(R.id.name);
                vLink  = (TextView) v.findViewById(R.id.link);
            }

        }

    }

}