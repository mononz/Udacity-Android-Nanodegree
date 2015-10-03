package net.mononz.nanodegree.movies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import net.mononz.nanodegree.movies.data.MoviesContract;
import net.mononz.nanodegree.movies.sync.Network;

public class MovieAdapter extends CursorAdapter {

    public static class ViewHolder {

        public final ImageView imageView;

        public ViewHolder(View view){
            imageView = (ImageView) view.findViewById(R.id.poster);
        }
    }

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent){
        int layoutId = R.layout.element_movie;
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor){
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        int imageIndex = cursor.getColumnIndex(MoviesContract.MovieEntry.COLUMN_POSTER);
        Glide.with(context)
                .load(Network.getImage(false, cursor.getString(imageIndex)))
                .centerCrop()
                .crossFade()
                .into(viewHolder.imageView);
    }

}