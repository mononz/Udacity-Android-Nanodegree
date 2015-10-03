package net.mononz.nanodegree.movies;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences_Manager {

    private static final String PREF_NAME = "preferences";

    private SharedPreferences main_pref;
    private SharedPreferences.Editor editor;

    public Preferences_Manager(Context context) {
        main_pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }


    // ---- Last Sync ---- //

    private static final String LAST_SYNC = "last_sync";

    public void setLastSync(long value) {
        save_to_preferences(main_pref, LAST_SYNC, value);
    }
    public long getLastSync() {
        return main_pref.getLong(LAST_SYNC, 0);
    }


    // ---- Sorting ---- //

    private static final String SORT_OPTION = "sort_option";
    public static final int SORT_FAVOURITES = 0;
    public static final int SORT_POPULARITY = 1;
    public static final int SORT_RATING = 2;

    public void setSortOption(int value) {
        save_to_preferences(main_pref, SORT_OPTION, value);
    }
    public int getSortOption() {
        return main_pref.getInt(SORT_OPTION, SORT_POPULARITY);
    }
    public String getSortOptionString() {
        switch (getSortOption()) {
            case SORT_FAVOURITES:
                return "Favourites";
            case SORT_POPULARITY:
                return "Sorted by Popularity";
            case SORT_RATING:
                return "Sorted by Rating";
            default:
                return null;
        }
    }


    // ---- Save Preferences ---- //

    private void save_to_preferences(SharedPreferences preference, String key, String value) {
        editor = preference.edit();
        editor.putString(key, value);
        editor.apply();
    }
    private void save_to_preferences(SharedPreferences preference, String key, int value) {
        editor = preference.edit();
        editor.putInt(key, value);
        editor.apply();
    }
    private void save_to_preferences(SharedPreferences preference, String key, boolean value) {
        editor = preference.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }
    private void save_to_preferences(SharedPreferences preference, String key, long value) {
        editor = preference.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public void clear_preference(SharedPreferences preference) {
        editor = preference.edit();
        editor.clear();
        editor.apply();
    }

}