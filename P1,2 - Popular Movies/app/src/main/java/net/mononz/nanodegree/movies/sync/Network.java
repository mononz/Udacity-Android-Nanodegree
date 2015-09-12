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

import java.io.IOException;

import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

public class Network {

    Retrofit restAdapter = new Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
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

}