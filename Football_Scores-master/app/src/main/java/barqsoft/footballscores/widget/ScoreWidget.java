package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.scoresAdapter;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by cristian on 18/10/15.
 */
public class ScoreWidget extends FootballWidget {

    public static final String LOG_TAG = ScoreWidget.class.getSimpleName();

    @Override
    public String getTag() {
        return LOG_TAG;
    }

    @Override
    public String getType() {
        return Utilies.WIDGET_TYPE_SCORE;
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
        Log.d(getTag(), "onUpdate()");
        for(int i=0; i<appWidgetIds.length; i++)
            updateWidget(context, appWidgetManager, appWidgetIds[i]);
    }

    public static void updateWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {

        Utilies.WidgetConfiguration config = Utilies.readWidgetConfiguration(context, Utilies.WIDGET_TYPE_SCORE, appWidgetId);
        if (config != null) {
            String[] data = new String[1];
            data[0] = Integer.toString(config.scoreId);
            Cursor cursor = context.getContentResolver().query(DatabaseContract.scores_table.buildScoreWithId(), null, null, data, null);
            if ((cursor != null) && (cursor.getCount() > 0)) {
                cursor.moveToFirst();

                final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.scores_widget_item);
                views.setTextViewText(R.id.home_name, cursor.getString(scoresAdapter.COL_HOME));
                views.setTextViewText(R.id.away_name, cursor.getString(scoresAdapter.COL_AWAY));
                views.setTextViewText(R.id.data_textview, cursor.getString(scoresAdapter.COL_MATCHTIME));
                views.setTextViewText(R.id.score_textview,
                        Utilies.getScores(cursor.getInt(scoresAdapter.COL_HOME_GOALS), cursor.getInt(scoresAdapter.COL_AWAY_GOALS)));
                views.setImageViewResource(R.id.home_crest,
                        Utilies.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_HOME)));
                views.setImageViewResource(R.id.away_crest,
                        Utilies.getTeamCrestByTeamName(cursor.getString(scoresAdapter.COL_AWAY)));

                final Intent intent = new Intent(context, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(DatabaseContract.scores_table.buildScoreWithId());
                Utilies.putWidgetConfiguration(intent, config);
                final PendingIntent pending = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.list_item, pending);

                appWidgetManager.updateAppWidget(appWidgetId, views);

                cursor.close();
            }
        }
        else Log.d(LOG_TAG, "No One Score configuration found for widgetId=" + Integer.toString(appWidgetId));

    }

}
