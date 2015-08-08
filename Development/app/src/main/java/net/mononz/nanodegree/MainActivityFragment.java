package net.mononz.nanodegree;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.gson.Gson;

import net.mononz.nanodegree.data.MoviesContract;
import net.mononz.nanodegree.json.Movies;
import net.mononz.nanodegree.json.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieAdapter mFlavorAdapter;

    private static final int CURSOR_LOADER_ID = 0;

    public MainActivityFragment() { }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Cursor c = getActivity().getContentResolver().query(MoviesContract.MovieEntry.CONTENT_URI,
                new String[]{MoviesContract.MovieEntry._ID},
                null, null, null);
        if (c.getCount() == 0){
            FetchMovies fetchMovies = new FetchMovies();
            fetchMovies.execute("popularity.desc");
        }
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
                DetailFragment detailFragment = DetailFragment.newInstance(uriId, uri);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, detailFragment)
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


    public class FetchMovies extends AsyncTask<String, Void, ArrayList<Result>> {

        @Override
        protected ArrayList<Result> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String strMovies = null;

            URL url = API.getPopularMovies(params[0]);
            if (url == null) {
                return null;
            }
            Log.d("url", url.toString());
            try {
                // Create the request to TMDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                strMovies = buffer.toString();

            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getMoviesFromString(strMovies);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Result> movies) {
            super.onPostExecute(movies);
            if (movies != null) {
                //adapter.clear();
                //adapter.addAll(strings);
                insertDataMovies(movies);
            }
        }
    }

    private ArrayList<Result> getMoviesFromString(String forecastJsonStr) {
        ArrayList<Result> content_list;
        try {
            Gson gson = new Gson();
            Movies movies = gson.fromJson(forecastJsonStr, Movies.class);
            content_list = movies.results;
        } catch (Exception e) {
            e.printStackTrace();
            content_list = new ArrayList<>();
        }
        return content_list;
    }

    public void insertDataMovies(ArrayList<Result> movies) {
        // Prepare for bulk insert of content values by creating content values array
        ContentValues[] movieValuesArr = new ContentValues[movies.size()];
        for (int i = 0; i < movies.size(); i++) {
            // Get Obj from GSON
            Obj_Movie movie = new Obj_Movie(movies.get(i).id,
                    bool2int(movies.get(i).adult),
                    movies.get(i).backdrop_path,
                    intArr2Str(movies.get(i).genre_ids),
                    movies.get(i).original_language,
                    movies.get(i).original_title,
                    movies.get(i).overview,
                    movies.get(i).release_date,
                    movies.get(i).poster_path,
                    movies.get(i).popularity,
                    movies.get(i).title,
                    bool2int(movies.get(i).video),
                    movies.get(i).vote_average,
                    movies.get(i).vote_count);

            // Convert Obj to a set of Content Values
            movieValuesArr[i] = new ContentValues();
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_ADULT, movie.adult);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_BACKDROP, movie.backdrop_path);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_GENRE, movie.genre_ids);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, movie.original_language);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, movie.original_title);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, movie.overview);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, movie.release_date);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_POSTER, movie.poster_path);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_POPULARITY, movie.popularity);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_TITLE, movie.title);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_VIDEO, movie.video);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, movie.vote_average);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, movie.vote_count);
        }
        getActivity().getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, movieValuesArr);
    }

    private int bool2int(boolean b) {
        return (b) ? 1 : 0;
    }

    private String intArr2Str(int[] arr) {
        String str = "";
        for (int i : arr) {
            str += i + ",";
        }
        str = str.replaceAll(",$", "");
        return str;
    }

}