package net.mononz.nanodegree.p1_movies;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.mononz.nanodegree.R;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;

public class FragmentMain extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = FragmentMain.class.getSimpleName();

    private MovieAdapter mFlavorAdapter;

    private static final String MOVIE_SORT = "sort";

    private static final int CURSOR_LOADER_ID = 0;

    public FragmentMain() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // initialize loader
        Bundle args = new Bundle();
        args.putString(MOVIE_SORT, MoviesContract.MovieEntry.SORT_POPULARITY);
        getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mFlavorAdapter = new MovieAdapter(getActivity(), null, 0, CURSOR_LOADER_ID);
        final GridView mGridView = (GridView) rootView.findViewById(R.id.flavors_grid);
        mGridView.setAdapter(mFlavorAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mFlavorAdapter.getItem(position);
                c.moveToPosition(position);
                int uriIndex = c.getColumnIndex(MoviesContract.MovieEntry._ID);
                int uriId = c.getInt(uriIndex);
                Log.i(LOG_TAG, "id reference extracted: " + uriId);

                Uri uri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, uriId);
                FragmentDetail fragmentDetail = FragmentDetail.newInstance(uriId, uri);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentDetail)
                        .addToBackStack(null)
                        .commit();
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        ((ActivityMovies) getActivity()).toolbar.setTitle(getString(R.string.btn_movies));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String sort_option;
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.sort_name:
                sort_option = MoviesContract.MovieEntry.SORT_TITLE;
                break;
            case R.id.sort_popularity:
                sort_option = MoviesContract.MovieEntry.SORT_POPULARITY;
                break;
            case R.id.sort_rating:
                sort_option = MoviesContract.MovieEntry.SORT_RATING;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        Bundle args = new Bundle();
        args.putString(MOVIE_SORT, sort_option);
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, args, this);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sort = null;
        if (args != null) {
            sort = args.getString(MOVIE_SORT);
        }
        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                null, null, null, sort);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mFlavorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFlavorAdapter.swapCursor(null);
    }

}