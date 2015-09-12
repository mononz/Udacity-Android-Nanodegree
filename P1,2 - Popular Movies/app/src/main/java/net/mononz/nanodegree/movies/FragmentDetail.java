package net.mononz.nanodegree.movies;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.mononz.nanodegree.movies.data.FavouritesContract;
import net.mononz.nanodegree.movies.data.MoviesContract;
import net.mononz.nanodegree.movies.sync.Network;

public class FragmentDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private Cursor mDetailCursor;
    private static final int CURSOR_LOADER_ID = 0;

    private int mMovieId;
    private String TAG_MOVIE_ID = "mMovieId";

    private ImageView mPoster;
    private TextView mPopularity;
    private TextView mRating;
    private TextView mPlot;
    private TextView mReleased;

    private boolean favourite;

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

        if (savedInstanceState != null) {
            // Restore last state
            mMovieId = savedInstanceState.getInt(TAG_MOVIE_ID);
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

        int posterIndex = mDetailCursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER);
        Glide.with(this)
                .load(Network.getImage(mDetailCursor.getString(posterIndex)))
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

        int createdDateIndex = mDetailCursor.getColumnIndex(FavouritesContract.FavouritesEntry.COLUMN_DATE_CREATED);
        favourite = !mDetailCursor.isNull(createdDateIndex);
        showFavouriteIcon();

        mReleased.setText(mDetailCursor.getString(releasedIndex));
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

}