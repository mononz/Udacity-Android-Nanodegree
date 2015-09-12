package net.mononz.nanodegree.movies;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

public class Network {

    RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.themoviedb.org/3/")
            .setLogLevel(RestAdapter.LogLevel.BASIC)
            .setConverter(new StringConverter())
            .build();

    public APIService service = restAdapter.create(APIService.class);

    public Network() {

    }

    public interface APIService {
        // https://api.themoviedb.org/3/discover/movie?api_key=XXX&sort_by=popularity.desc
        @GET("/discover/movie")
        void getMovies(
                @Query("api_key") String api_key,
                @Query("sort_by") String sort_by,
                Callback<String> callback);
    }

    public class StringConverter implements Converter {

        @Override
        public Object fromBody(TypedInput typedInput, Type type) throws ConversionException {
            String text = null;
            try {
                text = fromStream(typedInput.in());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return text;
        }

        @Override
        public TypedOutput toBody(Object o) {
            return null;
        }

        // Custom method to convert stream from request to string
        public String fromStream(InputStream in) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            return out.toString();
        }
    }

}