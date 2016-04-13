package net.mononz.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import net.mononz.xyzreader.R;
import net.mononz.xyzreader.data.ArticleLoader;

public final class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private Callback mCallback;

    public Adapter(Context mContext, Cursor cursor, Callback callback) {
        this.mContext = mContext;
        this.mCursor = cursor;
        this.mCallback = callback;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        final ViewHolder vh = new ViewHolder(itemView);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemClick(getItemId(vh.getAdapterPosition()));
            }
        });
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
        String subView = DateUtils.getRelativeTimeSpanString(mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE), System.currentTimeMillis(),
                DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString() + " by " + mCursor.getString(ArticleLoader.Query.AUTHOR);
        holder.subtitleView.setText(subView);

        holder.thumbnailView.setImageUrl(mCursor.getString(ArticleLoader.Query.THUMB_URL), ImageLoaderHelper.getInstance(mContext).getImageLoader());
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

        ImageLoaderHelper.getInstance(mContext).getImageLoader()
                .get(mCursor.getString(ArticleLoader.Query.THUMB_URL), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette p) {
                                    Palette.Swatch swatch = p.getVibrantSwatch();
                                    if (swatch != null) {
                                        Log.d("swatch", "" + swatch.getRgb());
                                        holder.article_footer.setBackgroundColor(swatch.getRgb());
                                        holder.titleView.setTextColor(swatch.getBodyTextColor());
                                        holder.subtitleView.setTextColor(swatch.getTitleTextColor());
                                    } else {
                                        Log.d("swatch", "null");
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    public final class ViewHolder extends RecyclerView.ViewHolder {

        public LinearLayout article_footer;
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);

            article_footer = (LinearLayout) view.findViewById(R.id.article_footer);
            thumbnailView = (DynamicHeightNetworkImageView) view.findViewById(R.id.thumbnail);
            titleView = (TextView) view.findViewById(R.id.article_title);
            subtitleView = (TextView) view.findViewById(R.id.article_subtitle);
        }
    }

    public interface Callback {
        void onItemClick(long id);
    }

}