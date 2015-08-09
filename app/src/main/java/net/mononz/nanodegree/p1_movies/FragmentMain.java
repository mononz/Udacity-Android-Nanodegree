package net.mononz.nanodegree.p1_movies;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import net.mononz.nanodegree.R;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;

public class FragmentMain extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = FragmentMain.class.getSimpleName();

    private MovieAdapter mFlavorAdapter;

    private static final int CURSOR_LOADER_ID = 0;

    public FragmentMain() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // initialize loader
        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setTitle("Popular Movies");

        mFlavorAdapter = new MovieAdapter(getActivity(), null, 0, CURSOR_LOADER_ID);
        GridView mGridView = (GridView) rootView.findViewById(R.id.flavors_grid);
        mGridView.setAdapter(mFlavorAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // increment the position to match Database Ids indexed starting at 1
                int uriId = position + 1;
                Uri uri = ContentUris.withAppendedId(MoviesContract.MovieEntry.CONTENT_URI, uriId);
                FragmentDetail fragmentDetail = FragmentDetail.newInstance(uriId, uri);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragmentDetail)
                        .addToBackStack(null).commit();
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                null, null, null, null);
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