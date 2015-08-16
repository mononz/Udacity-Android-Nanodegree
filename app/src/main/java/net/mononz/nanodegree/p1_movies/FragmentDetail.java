package net.mononz.nanodegree.p1_movies;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.mononz.nanodegree.R;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;
import net.mononz.nanodegree.p1_movies.sync.MovieSyncAdapter;

public class FragmentDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private Cursor mDetailCursor;
    private int mPosition;
    private Uri mUri;
    private static final int CURSOR_LOADER_ID = 0;

    private ImageView mPoster;
    private TextView mPopularity;
    private TextView mRating;
    private TextView mPlot;
    private TextView mReleased;

    public static FragmentDetail newInstance(int position, Uri uri) {
        FragmentDetail fragment = new FragmentDetail();
        Bundle args = new Bundle();
        fragment.mPosition = position;
        fragment.mUri = uri;
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentDetail() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        if (savedInstanceState != null) {
            // Restore last state
            mPosition = savedInstanceState.getInt("mPosition");
            mUri = Uri.parse(savedInstanceState.getString("mUri"));
            getLoaderManager().initLoader(CURSOR_LOADER_ID, new Bundle(), FragmentDetail.this);
        } else {
            Bundle args = this.getArguments();
            getLoaderManager().initLoader(CURSOR_LOADER_ID, args, FragmentDetail.this);
        }


        mPoster = (ImageView) rootView.findViewById(R.id.poster);
        mPopularity = (TextView) rootView.findViewById(R.id.popularity);
        mRating = (TextView) rootView.findViewById(R.id.rating);
        mPlot = (TextView) rootView.findViewById(R.id.plot);
        mReleased = (TextView) rootView.findViewById(R.id.released);

        Bundle args = this.getArguments();
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, FragmentDetail.this);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mPosition", mPosition);
        outState.putString("mUri", mUri.toString());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args){
        String selection = null;
        String [] selectionArgs = null;
        if (args != null) {
            selection = MoviesContract.MovieEntry._ID;
            selectionArgs = new String[]{String.valueOf(mPosition)};
        }
        return new CursorLoader(getActivity(),
                mUri, null, selection, selectionArgs, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);
    }

    // Set the cursor in our CursorAdapter once the Cursor is loaded
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDetailCursor = data;
        mDetailCursor.moveToFirst();
        DatabaseUtils.dumpCursor(data);

        int nameIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
        ((ActivityMovies) getActivity()).toolbar.setTitle(mDetailCursor.getString(nameIndex));

        int posterIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER);
        Glide.with(this)
                .load(MovieSyncAdapter.getImage(mDetailCursor.getString(posterIndex)))
                .into(mPoster);

        int popularityIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POPULARITY);
        mPopularity.setText(mDetailCursor.getString(popularityIndex));

        int ratingAverageIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        int ratingCountIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT);
        mRating.setText("" + mDetailCursor.getDouble(ratingAverageIndex) + " (" + mDetailCursor.getInt(ratingCountIndex) + ")");

        int plotIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_OVERVIEW);
        mPlot.setText(mDetailCursor.getString(plotIndex));

        int releasedIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE);
        mReleased.setText(mDetailCursor.getString(releasedIndex));

    }

    // reset CursorAdapter on Loader Reset
    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        mDetailCursor = null;
    }

}