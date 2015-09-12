package net.mononz.nanodegree.movies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import net.mononz.nanodegree.movies.Preferences_Manager;
import net.mononz.nanodegree.movies.R;
import net.mononz.nanodegree.movies.api.Movies;
import net.mononz.nanodegree.movies.api.MoviesResult;
import net.mononz.nanodegree.movies.data.MoviesContract;
import net.mononz.nanodegree.movies.data.Obj_Movie;

import java.util.ArrayList;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;

public class MovieSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String LOG_TAG = MovieSyncAdapter.class.getSimpleName();

    private static final String IMAGE_QUALITY = "w342"; // "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/" + IMAGE_QUALITY;

    public MovieSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        getPopularMovies();
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Log.d(LOG_TAG, "syncImmediately");
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        // Create the account type and default account
        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {
            // Add the account and account type, no password or user data If successful, return the Account object, otherwise report an error.
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount) {
        Log.d(LOG_TAG, "Account created - " + newAccount.name);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    // API request for Top 20 movies from TMDB
    public void getPopularMovies() {
        Log.d(LOG_TAG, "getPopularMovies");
        Call<Movies> call = new Network().service.getMovies(getContext().getString(R.string.tmdb_api_key), "popularity.desc");
        call.enqueue(new Callback<Movies>() {
            @Override
            public void onResponse(Response<Movies> response) {
                Log.d("onResponse", "" + response.code() + " " + response.message());
                if (response.isSuccess()) {
                    insertDataMovies(response.body());
                } else {
                    Network.handleErrorResponse(getContext(), response);
                }
            }
            @Override
            public void onFailure(Throwable t) {
                Log.e(LOG_TAG, t.toString());
            }
        });
    }

    // Bulk Insert movies into db
    public void insertDataMovies(Movies movie) {
        ArrayList<MoviesResult> movies = movie.results;
        ContentValues[] movieValuesArr = new ContentValues[movies.size()];
        for (int i = 0; i < movies.size(); i++) {
            // Build Object from GSON
            Obj_Movie obj_movie = new Obj_Movie(
                    bool2int(movies.get(i).adult),
                    movies.get(i).backdrop_path,
                    intArr2Str(movies.get(i).genre_ids),
                    movies.get(i).id,
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

            // Convert Object to a set of Content Values
            movieValuesArr[i] = new ContentValues();
            movieValuesArr[i].put(MoviesContract.MovieEntry._ID, obj_movie.id);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_ADULT, obj_movie.adult);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_BACKDROP, obj_movie.backdrop_path);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_GENRE, obj_movie.genre_ids);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE, obj_movie.original_language);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE, obj_movie.original_title);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, obj_movie.overview);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, obj_movie.release_date);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_POSTER, obj_movie.poster_path);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_POPULARITY, obj_movie.popularity);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_TITLE, obj_movie.title);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_VIDEO, obj_movie.video);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE, obj_movie.vote_average);
            movieValuesArr[i].put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, obj_movie.vote_count);
        }
        Log.d(LOG_TAG, "NumMovies " + movieValuesArr.length);

        // New content values -> Drop the old records and bulk insert new movies
        getContext().getContentResolver().delete(MoviesContract.MovieEntry.CONTENT_URI, null, null);
        getContext().getContentResolver().bulkInsert(MoviesContract.MovieEntry.CONTENT_URI, movieValuesArr);
        Log.d(LOG_TAG, "Sync completed + movies updated");

        // Store last sync time
        Preferences_Manager preferences_manager = new Preferences_Manager(getContext());
        preferences_manager.setLastSync(System.currentTimeMillis());
    }


    // HELPER FUNCTIONS

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