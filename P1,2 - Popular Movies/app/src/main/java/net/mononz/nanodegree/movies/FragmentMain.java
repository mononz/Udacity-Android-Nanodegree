package net.mononz.nanodegree.movies;

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
import android.widget.AdapterView;
import android.widget.GridView;

import net.mononz.nanodegree.movies.data.FavouritesContract;
import net.mononz.nanodegree.movies.data.MoviesContract;

public class FragmentMain extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private MovieAdapter mFlavorAdapter;
    private Preferences_Manager preferences_manager;

    private static final int CURSOR_LOADER_ID = 0;

    private Callbacks mCallbacks = sDummyCallbacks;
    public interface Callbacks {
        void onItemSelected(int id);
        void onUpdateToolbar(String str);
    }

    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int id) {}
        @Override
        public void onUpdateToolbar(String str) {}
    };

    public FragmentMain() { }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // initialize loader
        preferences_manager = new Preferences_Manager(getActivity());

        getLoaderManager().initLoader(CURSOR_LOADER_ID, new Bundle(), this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mCallbacks = (Callbacks) getActivity();

        mFlavorAdapter = new MovieAdapter(getActivity(), null, 0);
        final GridView mGridView = (GridView) rootView.findViewById(R.id.flavors_grid);
        mGridView.setAdapter(mFlavorAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mFlavorAdapter.getItem(position);
                c.moveToPosition(position);
                int uriIndex = c.getColumnIndex(MoviesContract.MovieEntry._ID);
                int uriId = c.getInt(uriIndex);
                mCallbacks.onItemSelected(uriId);
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        switch (preferences_manager.getSortOption()) {
            case Preferences_Manager.SORT_FAVOURITES:
                menu.findItem(R.id.sort_favourites).setChecked(true);
                break;
            case Preferences_Manager.SORT_POPULARITY:
                menu.findItem(R.id.sort_popularity).setChecked(true);
                break;
            case Preferences_Manager.SORT_RATING:
                menu.findItem(R.id.sort_rating).setChecked(true);
                break;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_favourites:
                item.setChecked(!item.isChecked());
                preferences_manager.setSortOption(Preferences_Manager.SORT_FAVOURITES);
                break;
            case R.id.sort_popularity:
                item.setChecked(!item.isChecked());
                preferences_manager.setSortOption(Preferences_Manager.SORT_POPULARITY);
                break;
            case R.id.sort_rating:
                item.setChecked(!item.isChecked());
                preferences_manager.setSortOption(Preferences_Manager.SORT_RATING);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, new Bundle(), this);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String sort_option = MoviesContract.MovieEntry.SORT_POPULARITY;
        switch (preferences_manager.getSortOption()) {
            case Preferences_Manager.SORT_FAVOURITES:
                selection = FavouritesContract.FavouritesEntry.FULL_ID;
                sort_option = MoviesContract.MovieEntry.SORT_TITLE;
                break;
            case Preferences_Manager.SORT_POPULARITY:
                sort_option = MoviesContract.MovieEntry.SORT_POPULARITY + " LIMIT 20";
                break;
            case Preferences_Manager.SORT_RATING:
                sort_option = MoviesContract.MovieEntry.SORT_RATING + " LIMIT 20";
                break;
        }
        mCallbacks.onUpdateToolbar(preferences_manager.getSortOptionString());

        return new CursorLoader(getActivity(),
                MoviesContract.MovieEntry.CONTENT_URI,
                new String[]{MoviesContract.MovieEntry.TABLE_MOVIES + ".*", FavouritesContract.FavouritesEntry.COLUMN_DATE_CREATED},
                selection, null, sort_option);
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