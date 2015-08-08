package net.mononz.nanodegree;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public class API {

    // Insert api key here
    private static final String API_KEY = "XXXX";

    private static final String IMAGE_QUALITY = "w500"; // "w92", "w154", "w185", "w342", "w500", "w780", or "original"

    private static final String BASE_URL = "https://api.themoviedb.org/3";
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/" + IMAGE_QUALITY;

    private static final String PARAM_API = "api_key";
    private static final String PARAM_SORT = "sort_by";

    // API request for Top 20 movies from TMDB
    public static URL getPopularMovies(String sort_param) {
        // https://api.themoviedb.org/3/discover/movie?api_key=XXX&sort_by=popularity.desc
        try {
            Uri builtUri = Uri.parse(BASE_URL + "/discover/movie?").buildUpon()
                    .appendQueryParameter(PARAM_API, API_KEY)
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

}