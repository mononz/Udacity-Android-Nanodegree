package net.mononz.nanodegree.p1_movies.api;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import net.mononz.nanodegree.R;
import net.mononz.nanodegree.p1_movies.data.Obj_Movie;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Api extends AsyncTask<String, Void, ArrayList<MoviesResult>> {

    private static final String IMAGE_QUALITY = "w342"; // "w92", "w154", "w185", "w342", "w500", "w780", or "original"

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/" + IMAGE_QUALITY;

    private static final String PARAM_API = "api_key";
    private static final String PARAM_SORT = "sort_by";

    private Context context;

    public Api(Context context) {
        this.context = context;
    }

    @Override
    protected ArrayList<MoviesResult> doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String strMovies = null;
        URL url = getPopularMovies(context.getString(R.string.tmdb_api_key), params[0]);
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
    protected void onPostExecute(ArrayList<MoviesResult> movies) {
        super.onPostExecute(movies);
        if (movies != null) {
            insertDataMovies(movies);
        }
    }

    // Decode Json to ArrayList from downloaded string
    private ArrayList<MoviesResult> getMoviesFromString(String forecastJsonStr) {
        ArrayList<MoviesResult> content_list;
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

    // Bulk Insert movies into db
    public void insertDataMovies(ArrayList<MoviesResult> movies) {
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
        context.getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, movieValuesArr);
    }

    // API request for Top 20 movies from TMDB
    public static URL getPopularMovies(String api_key, String sort_param) {
        // https://api.themoviedb.org/3/discover/movie?api_key=XXX&sort_by=popularity.desc
        try {
            Uri builtUri = Uri.parse(BASE_URL + "/discover/movie?").buildUpon()
                    .appendQueryParameter(PARAM_API, api_key)
                    .appendQueryParameter(PARAM_SORT, sort_param)
                    .build();
            return new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    // API request for image (banner/poster/etc..) from TMDB
    public static String getImage(String image_id) {
        // http://image.tmdb.org/t/p/w500/8uO0gUM8aNqYLs1OsTBQiXu0fEv.jpg
        return IMAGE_URL + image_id;
    }

    // Helper function for storing booleans as integers in db
    private int bool2int(boolean b) {
        return (b) ? 1 : 0;
    }

    // Converting an integer array to a comma separated list for db
    private String intArr2Str(int[] arr) {
        String str = "";
        for (int i : arr) {
            str += i + ",";
        }
        str = str.replaceAll(",$", "");
        return str;
    }

}