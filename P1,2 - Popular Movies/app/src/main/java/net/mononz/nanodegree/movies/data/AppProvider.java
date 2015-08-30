package net.mononz.nanodegree.movies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

public class AppProvider extends ContentProvider {

    public static final String CONTENT_AUTHORITY = "net.mononz.nanodegree.movies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String LOG_TAG = AppProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private AppDBHelper mOpenHelper;

    private static final int MOVIE = 100;
    private static final int MOVIES_WITH_FAVOURITES = 200;
    private static final int MOVIE_WITH_ID = 300;
    private static final int FAVOURITE = 400;
    private static final int FAVOURITE_WITH_ID = 500;

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES, MOVIE);
        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES, MOVIES_WITH_FAVOURITES);
        matcher.addURI(authority, MoviesContract.MovieEntry.TABLE_MOVIES + "/#", MOVIE_WITH_ID);

        matcher.addURI(authority, FavouritesContract.FavouritesEntry.TABLE_FAVOURITES, FAVOURITE);
        matcher.addURI(authority, FavouritesContract.FavouritesEntry.TABLE_FAVOURITES + "/#", FAVOURITE_WITH_ID);
        return matcher;
    }

    @Override
    public boolean onCreate(){
        mOpenHelper = new AppDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri){
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case MOVIE:
                return MoviesContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIES_WITH_FAVOURITES:
                return MoviesContract.MovieEntry.CONTENT_DIR_TYPE;
            case MOVIE_WITH_ID:
                return MoviesContract.MovieEntry.CONTENT_ITEM_TYPE;
            case FAVOURITE:
                return FavouritesContract.FavouritesEntry.CONTENT_DIR_TYPE;
            case FAVOURITE_WITH_ID:
                return FavouritesContract.FavouritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder){
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // All Movies selected
            case MOVIE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_MOVIES,
                        projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            // All Movies selected
            case MOVIES_WITH_FAVOURITES:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.MovieEntry.TABLE_MOVIES,
                        projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            // Individual movie based on Id selected
            case MOVIE_WITH_ID:
                SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
                qb.setTables(MoviesContract.MovieEntry.TABLE_MOVIES +
                        " LEFT JOIN " + FavouritesContract.FavouritesEntry.TABLE_FAVOURITES + " ON " + MoviesContract.MovieEntry.FULL_ID + "=" + FavouritesContract.FavouritesEntry.FULL_ID);
                retCursor = qb.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        MoviesContract.MovieEntry.FULL_ID + "=?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null, null, sortOrder);
                break;
            // All Favourites added
            case FAVOURITE:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavouritesContract.FavouritesEntry.TABLE_FAVOURITES,
                        projection,
                        selection, selectionArgs,
                        null, null, sortOrder);
                break;
            // Individual favourite based on Id selected
            case FAVOURITE_WITH_ID:
                retCursor = mOpenHelper.getReadableDatabase().query(
                        FavouritesContract.FavouritesEntry.TABLE_FAVOURITES,
                        projection,
                        FavouritesContract.FavouritesEntry._ID + "=?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case MOVIE: {
                long _id = db.insert(MoviesContract.MovieEntry.TABLE_MOVIES, null, values);
                // insert unless it is already contained in the database
                if (_id > 0) {
                    returnUri = MoviesContract.MovieEntry.buildMoviesUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            case FAVOURITE: {
                long _id = db.insert(FavouritesContract.FavouritesEntry.TABLE_FAVOURITES, null, values);
                if (_id > 0) {
                    returnUri = FavouritesContract.FavouritesEntry.buildMoviesUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d("Insert", "notify changed");
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted;
        switch(match){
            case MOVIE:
                numDeleted = db.delete(MoviesContract.MovieEntry.TABLE_MOVIES, selection, selectionArgs);
                break;
            case MOVIE_WITH_ID:
                numDeleted = db.delete(MoviesContract.MovieEntry.TABLE_MOVIES,
                        MoviesContract.MovieEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            case FAVOURITE:
                numDeleted = db.delete(FavouritesContract.FavouritesEntry.TABLE_FAVOURITES, selection, selectionArgs);
                break;
            case FAVOURITE_WITH_ID:
                numDeleted = db.delete(FavouritesContract.FavouritesEntry.TABLE_FAVOURITES,
                        FavouritesContract.FavouritesEntry._ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("Delete", "notify changed (" + numDeleted + ")");
        }
        return numDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numInserted;
        switch(match){
            case MOVIE:
                db.beginTransaction();
                numInserted = 0;
                try {
                    for(ContentValues value : values){
                        if (value == null) {
                            throw new IllegalArgumentException("Cannot have null content values");
                        }
                        long _id = -1;
                        try {
                            _id = db.insertOrThrow(MoviesContract.MovieEntry.TABLE_MOVIES, null, value);
                        } catch(SQLiteConstraintException e) {
                            Log.w(LOG_TAG, "Attempting to insert " + value.getAsString(MoviesContract.MovieEntry.COLUMN_TITLE) + " but value is already in database.");
                        }
                        if (_id != 1) {
                            numInserted++;
                        }
                    }
                    if (numInserted > 0) {
                        db.setTransactionSuccessful();
                    }
                } finally {
                    db.endTransaction();
                }
                if (numInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    Log.d("Bulk Insert", "notify changed (" + numInserted + ")");
                }
                return numInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int numUpdated;
        if (contentValues == null) {
            throw new IllegalArgumentException("Cannot have null content values");
        }
        switch (sUriMatcher.match(uri)) {
            case MOVIE:
                numUpdated = db.update(MoviesContract.MovieEntry.TABLE_MOVIES,
                        contentValues,
                        selection,
                        selectionArgs);
                break;
            case MOVIE_WITH_ID:
                numUpdated = db.update(MoviesContract.MovieEntry.TABLE_MOVIES,
                        contentValues,
                        MoviesContract.MovieEntry._ID + "=?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            case FAVOURITE_WITH_ID:
                numUpdated = db.update(FavouritesContract.FavouritesEntry.TABLE_FAVOURITES,
                        contentValues,
                        FavouritesContract.FavouritesEntry._ID + "=?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (numUpdated > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            Log.d("Update", "notify changed (" + numUpdated + ")");
        }
        return numUpdated;
    }

}
