package net.mononz.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

import net.mononz.xyzreader.R;
import net.mononz.xyzreader.data.ArticleLoader;
import net.mononz.xyzreader.data.CursorRecAdapter;

public final class AdapterArticleList extends CursorRecAdapter<AdapterArticleList.ViewHolder> {

    private Context mContext;
    private Callback mCallback;

    public AdapterArticleList(Context mContext, Cursor cursor, Callback callback) {
        super(cursor);
        this.mContext = mContext;
        this.mCallback = callback;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {

        holder.titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));

        String subView = DateUtils.getRelativeTimeSpanString(cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE), System.currentTimeMillis(),
                DateUtils.HOUR_IN_MILLIS, DateUtils.FORMAT_ABBREV_ALL).toString() + " by " + cursor.getString(ArticleLoader.Query.AUTHOR);
        holder.subtitleView.setText(subView);

        holder.thumbnailView.setImageUrl(cursor.getString(ArticleLoader.Query.THUMB_URL), ImageLoaderHelper.getInstance(mContext).getImageLoader());
        holder.thumbnailView.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

        ImageLoaderHelper.getInstance(mContext).getImageLoader()
                .get(cursor.getString(ArticleLoader.Query.THUMB_URL), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette p) {
                                    Palette.Swatch swatch = p.getMutedSwatch();
                                    if (swatch != null) {
                                        holder.article_footer.setBackgroundColor(swatch.getRgb());
                                        holder.titleView.setTextColor(swatch.getBodyTextColor());
                                        holder.subtitleView.setTextColor(swatch.getTitleTextColor());
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onItemClick(getItemId(holder.getAdapterPosition()));
            }
        });
    }

    public final class ViewHolder extends RecyclerView.ViewHolder {

        public View mView;
        public LinearLayout article_footer;
        public DynamicHeightNetworkImageView thumbnailView;
        public TextView titleView;
        public TextView subtitleView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
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