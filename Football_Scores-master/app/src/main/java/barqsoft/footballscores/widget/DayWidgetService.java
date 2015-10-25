package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.R;
import barqsoft.footballscores.scoresAdapter;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by cristian on 25/10/15.
 */

public class DayWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DayRemoteViewsFactory(this.getApplicationContext(), intent);
    }

}

class DayRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public static final String LOG_TAG = DayRemoteViewsFactory.class.getSimpleName();

    private Context mContext;
    private int mAppWidgetId;

    private Utilies.WidgetConfiguration config = null;
    private String[] dateAsText = null;
    private Cursor cursor = null;

    public DayRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        config = Utilies.readWidgetConfiguration(mContext, Utilies.WIDGET_TYPE_DAY, mAppWidgetId);
        if (config != null) {
            dateAsText = new String[1];
            dateAsText[0] = Utilies.formatDate(config.date);
            Log.d(LOG_TAG,"DayRemoteViewsFactory(" + dateAsText + ")");
        } else Log.d(LOG_TAG, "DayRemoteViewsFactory()");
    }

    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        //onDataSetChanged();
    }

    public void onDestroy() {
        Log.d(LOG_TAG,"onDestroy()");
        if (cursor != null) cursor.close();
    }

    public int getCount() {
        return (cursor != null)? cursor.getCount() : 0;
    }

    public RemoteViews getViewAt(int position) {
        if (!cursor.moveToPosition(position)) return null;

        final RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.day_widget_item);
        views.setTextViewText(R.id.home_name, cursor.getString(scoresAdapter.COL_HOME));
        views.setTextViewText(R.id.away_name, cursor.getString(scoresAdapter.COL_AWAY));
        views.setTextViewText(R.id.data_textview, cursor.getString(scoresAdapter.COL_MATCHTIME));
        views.setTextViewText(R.id.score_textview,
                Utilies.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS), cursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
        views.setImageViewResource(R.id.home_crest,
                Utilies.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_HOME)));
        views.setImageViewResource(R.id.away_crest,
                Utilies.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_AWAY)));

        Intent fillInIntent = new Intent();
        Utilies.putWidgetConfiguration(fillInIntent,
                new Utilies.WidgetConfiguration(config.date, cursor.getInt(scoresAdapter.COL_ID)));

        views.setOnClickFillInIntent(R.id.list_item, fillInIntent);

        return views;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        if ((cursor != null) && (cursor.getCount() > 0) && cursor.moveToPosition(position))
            return cursor.getInt(scoresAdapter.COL_ID);
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        Log.d(LOG_TAG,"onDataSetChanged()");
        if (cursor != null) cursor.close();
        cursor = mContext.getContentResolver().query(
                DatabaseContract.scores_table.buildScoreWithDate(), null, null, dateAsText, null);
    }

}

