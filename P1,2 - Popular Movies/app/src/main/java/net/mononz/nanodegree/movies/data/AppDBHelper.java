package net.mononz.nanodegree.movies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AppDBHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = AppDBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "nanodegree.db";
    private static final int DATABASE_VERSION = 2;
    public AppDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MoviesContract.MovieEntry.TABLE_MOVIES + " (" +
                MoviesContract.MovieEntry._ID + " INTEGER PRIMARY KEY, " +
                MoviesContract.MovieEntry.COLUMN_ADULT	+ " INTEGER NOT NULL DEFAULT 0, " +
                MoviesContract.MovieEntry.COLUMN_BACKDROP + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_GENRE + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_ORIGINAL_TITLE+ " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_POPULARITY + " REAL NOT NULL DEFAULT 0, " +
                MoviesContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.MovieEntry.COLUMN_VIDEO + " INTEGER NOT NULL DEFAULT 0, " +
                MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL DEFAULT 0, " +
                MoviesContract.MovieEntry.COLUMN_VOTE_COUNT + " INTEGER NOT NULL DEFAULT 0);";
        sqLiteDatabase.execSQL(SQL_CREATE_MOVIE_TABLE);

        final String SQL_CREATE_FAVOURITES_TABLE = "CREATE TABLE " + FavouritesContract.FavouritesEntry.TABLE_FAVOURITES + " (" +
                FavouritesContract.FavouritesEntry._ID	+ " INTEGER PRIMARY KEY, " +
                FavouritesContract.FavouritesEntry.COLUMN_DATE_CREATED + " TIMESTAMP NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.MovieEntry.TABLE_MOVIES);
        //sqLiteDatabase.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + MoviesContract.MovieEntry.TABLE_MOVIES + "'");

        final String SQL_CREATE_FAVOURITES_TABLE = "CREATE TABLE IF NOT EXISTS " + FavouritesContract.FavouritesEntry.TABLE_FAVOURITES + " (" +
                FavouritesContract.FavouritesEntry._ID	+ " INTEGER PRIMARY KEY, " +
                FavouritesContract.FavouritesEntry.COLUMN_DATE_CREATED + " TIMESTAMP NOT NULL);";
        sqLiteDatabase.execSQL(SQL_CREATE_FAVOURITES_TABLE);

        //onCreate(sqLiteDatabase);
    }
}