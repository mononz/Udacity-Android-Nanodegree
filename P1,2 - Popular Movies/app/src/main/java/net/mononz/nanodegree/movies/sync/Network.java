package net.mononz.nanodegree.movies.sync;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.ResponseBody;

import net.mononz.nanodegree.movies.api.ErrorCode;
import net.mononz.nanodegree.movies.api.movies.Movies;
import net.mononz.nanodegree.movies.api.reviews.Reviews;
import net.mononz.nanodegree.movies.api.videos.Videos;

import java.io.IOException;

import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public class Network {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";

    // "w92", "w154", "w185", "w342", "w500", "w780", or "original"
    private static final String IMAGE_QUALITY_HIGH = "w780";
    private static final String IMAGE_QUALITY_LOW = "w342";
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/";


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
                @Path("id") int id,
                @Query("api_key") String api_key);

        // https://api.themoviedb.org/3/movie/102899/videos?api_key=XXX
        @GET("movie/{id}/videos")
        Call<Videos> getMovieVideos(
                @Path("id") int id,
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
    @Nullable
    public static String handleErrorResponse(Response response) {
        try {
            Gson gson = new Gson();
            ErrorCode err = gson.fromJson(response.errorBody().string(), ErrorCode.class);
            return err.status_message;
        } catch (IOException e) {
            Log.e("IOException", e.toString());
            return null;
        }
    }

    // API request for image (banner/poster/etc..) from TMDB
    public static String getImage(boolean highQuality, String image_path) {
        // http://image.tmdb.org/t/p/w500/xu9zaAevzQ5nnrsXN6JcahLnG4i.jpg
        return IMAGE_URL + ((highQuality) ? IMAGE_QUALITY_HIGH : IMAGE_QUALITY_LOW) + image_path;
    }

}