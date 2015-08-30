package net.mononz.nanodegree.movies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class FavouritesContract {

    public static final class FavouritesEntry implements BaseColumns {

        // table name
        public static final String TABLE_FAVOURITES = "favourites";
        // columns
        public static final String _ID = "_id";
        public static final String COLUMN_DATE_CREATED = "date_created";

        public static final String FULL_ID = TABLE_FAVOURITES + "." + _ID;

        // create content uri
        public static final Uri CONTENT_URI = AppProvider.BASE_CONTENT_URI.buildUpon().appendPath(TABLE_FAVOURITES).build();
        // create cursor of base type directory for multiple entries
        public static final String CONTENT_DIR_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AppProvider.CONTENT_AUTHORITY + "/" + TABLE_FAVOURITES;
        // create cursor of base type item for single entry
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + AppProvider.CONTENT_AUTHORITY + "/" + TABLE_FAVOURITES;

        // for building URIs on insertion
        public static Uri buildMoviesUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}