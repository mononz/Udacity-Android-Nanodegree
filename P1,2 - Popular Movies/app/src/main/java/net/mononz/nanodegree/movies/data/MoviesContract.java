package net.mononz.nanodegree.movies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MoviesContract {

    public static final class MovieEntry implements BaseColumns {

        // table name
        public static final String TABLE_MOVIES = "movie";
        // columns
        public static final String _ID = "_id";
        public static final String COLUMN_ADULT = "adult";
        public static final String COLUMN_BACKDROP = "backdrop_path";
        public static final String COLUMN_GENRE = "genre_ids";
        public static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER = "poster_path";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VIDEO = "video";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VOTE_COUNT = "vote_count";

        public static final String FULL_ID = TABLE_MOVIES + "." + _ID;

        // create content uri
        public static final Uri CONTENT_URI = AppProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_MOVIES).build();
        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AppProvider.CONTENT_AUTHORITY + "/" + TABLE_MOVIES;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + AppProvider.CONTENT_AUTHORITY + "/" + TABLE_MOVIES;

        // for building URIs on insertion
        public static Uri buildMoviesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static final String SORT_TITLE = MoviesContract.MovieEntry.COLUMN_TITLE + " COLLATE NOCASE ASC";
        public static final String SORT_POPULARITY = MoviesContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        public static final String SORT_RATING = MoviesContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";

    }
}