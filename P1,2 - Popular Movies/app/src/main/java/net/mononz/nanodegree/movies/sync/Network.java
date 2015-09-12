package net.mononz.nanodegree.movies.sync;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import net.mononz.nanodegree.movies.api.ErrorCode;
import net.mononz.nanodegree.movies.api.Movies;
import net.mononz.nanodegree.movies.api.Reviews;
import net.mononz.nanodegree.movies.api.Videos;

import java.io.IOException;

import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

public class Network {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    private static final String IMAGE_QUALITY = "w342"; // "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/" + IMAGE_QUALITY;


    Retrofit restAdapter = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverter(String.class, new ToStringConverter())
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public APIService service = restAdapter.create(APIService.class);

    public Network() { }

    public interface APIService {

        // https://api.themoviedb.org/3/discover/movie?api_key=XXX&sort_by=popularity.desc
        @GET("discover/movie")
        Call<Movies> getMovies(
                @Query("api_key") String api_key,
                @Query("sort_by") String sort_by);

        // https://api.themoviedb.org/3/movie/102899/reviews?api_key=XXX
        @GET("movie/{id}/reviews")
        Call<Reviews> getMovieReviews(
                @Query("id") String id,
                @Query("api_key") String api_key);

        // https://api.themoviedb.org/3/movie/102899/videos?api_key=XXX
        @GET("movie/{id}/videos")
        Call<Videos> getMovieVideos(
                @Query("id") String id,
                @Query("api_key") String api_key);


    }

    // Custom String Converter for Retrofit 2
    public final class ToStringConverter implements Converter<String> {

        @Override
        public String fromBody(ResponseBody body) throws IOException {
            return body.string();
        }

        @Override
        public RequestBody toBody(String value) {
            return RequestBody.create(MediaType.parse("text/plain"), value);
        }
    }

    // Handle generic error response json from tmdb
    public static void handleErrorResponse(Context context, Response response) {
        try {
            Gson gson = new Gson();
            ErrorCode err = gson.fromJson(response.errorBody().string(), ErrorCode.class);
            Toast.makeText(context, err.status_message, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("IOException", e.toString());
        }
    }

    // API request for image (banner/poster/etc..) from TMDB
    public static String getImage(String image_id) {
        // http://image.tmdb.org/t/p/w500/8uO0gUM8aNqYLs1OsTBQiXu0fEv.jpg
        return IMAGE_URL + image_id;
    }

}