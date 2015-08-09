package net.mononz.nanodegree.p1_movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.mononz.nanodegree.R;
import net.mononz.nanodegree.p1_movies.api.Api;
import net.mononz.nanodegree.p1_movies.data.MoviesContract;

public class MovieAdapter extends CursorAdapter {

    private static final String LOG_TAG = MovieAdapter.class.getSimpleName();
    private Context mContext;
    private static int sLoaderID;

    public static class ViewHolder {

        public final ImageView imageView;
        //public final TextView textView;

        public ViewHolder(View view){
            imageView = (ImageView) view.findViewById(R.id.flavor_image);
            //textView = (TextView) view.findViewById(R.id.flavor_text);
        }
    }

    public MovieAdapter(Context context, Cursor c, int flags, int loaderID){
        super(context, c, flags);
        Log.d(LOG_TAG, "MovAdapter");
        mContext = context;
        sLoaderID = loaderID;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        int layoutId = R.layout.element_movie;
        Log.d(LOG_TAG, "In new View");

        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        Log.d(LOG_TAG, "In bind View");

        //int versionIndex = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_TITLE);
        //final String versionName = cursor.getString(versionIndex);
        //viewHolder.textView.setText(versionName);

        int imageIndex = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER);
        Log.i(LOG_TAG, "Image reference extracted: " + cursor.getString(imageIndex));

        Glide.with(context)
                .load(Api.getImage(cursor.getString(imageIndex)))
                .centerCrop()
                //.placeholder(R.drawable.cupcake)
                .crossFade()
                .into(viewHolder.imageView);

    }

}