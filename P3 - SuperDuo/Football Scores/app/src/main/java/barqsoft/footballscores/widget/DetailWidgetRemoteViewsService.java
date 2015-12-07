package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.scoresAdapter;

/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {

    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {

            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();

                Date fragmentdate = new Date(System.currentTimeMillis()); // today scores
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                String date = mformat.format(fragmentdate);

                String[] fragdate = new String[1];
                fragdate[0] = date;

                data = getContentResolver().query(DatabaseContract.scores_table.buildScoreWithDate(), null, null, fragdate, null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION || data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.element_widget);
                data.moveToPosition(position);

                views.setTextViewText(R.id.home_name, data.getString(scoresAdapter.COL_HOME));
                views.setTextViewText(R.id.away_name, data.getString(scoresAdapter.COL_AWAY));
                views.setTextViewText(R.id.data_textview, data.getString(scoresAdapter.COL_MATCHTIME));
                views.setTextViewText(R.id.score_textview, Utilities.getScores(data.getInt(scoresAdapter.COL_HOME_GOALS), data.getInt(scoresAdapter.COL_AWAY_GOALS)));
                //views.setTextViewText(R.id.match_id, data.getDouble(scoresAdapter.COL_ID);
                views.setImageViewResource(R.id.home_crest, Utilities.getTeamCrestByTeamName(data.getString(scoresAdapter.COL_HOME)));
                views.setImageViewResource(R.id.away_crest, Utilities.getTeamCrestByTeamName(data.getString(scoresAdapter.COL_AWAY)));

                views.setOnClickFillInIntent(R.id.widget_list_item, new Intent());
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.element_widget);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(0);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
