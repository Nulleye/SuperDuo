package barqsoft.footballscores.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;
import barqsoft.footballscores.data.DatabaseContract;

/**
 * Created by cristian on 18/10/15.
 */
public class DayWidget extends FootballWidget {

    public static final String LOG_TAG = DayWidget.class.getSimpleName();

    @Override
    public String getTag() {
        return LOG_TAG;
    }

    @Override
    public String getType() {
        return Utilies.WIDGET_TYPE_DAY;
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager,int[] appWidgetIds) {
        Log.d(getTag(), "onUpdate()");
        for(int i=0; i<appWidgetIds.length; i++)
            updateWidget(context, appWidgetManager, appWidgetIds[i]);
    }

    public static void updateWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {

        final Utilies.WidgetConfiguration config = Utilies.readWidgetConfiguration(context, Utilies.WIDGET_TYPE_DAY, appWidgetId);
        if (config != null) {

            final Intent intent = new Intent(context, DayWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.day_widget_list);
            rv.setRemoteAdapter(appWidgetId, R.id.scores_list, intent);
            rv.setEmptyView(R.id.scores_list, R.id.empty_view);

            rv.setTextViewText(R.id.day_text,Utilies.getDayName(context, config.date.getTime()));

            final Intent launchIntent = new Intent(context, MainActivity.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.setData(DatabaseContract.scores_table.buildScoreWithId());
            Utilies.putWidgetConfiguration(launchIntent, config);
            launchIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            launchIntent.setData(Uri.parse(launchIntent.toUri(Intent.URI_INTENT_SCHEME)));

            final PendingIntent pending = PendingIntent.getActivity(context, appWidgetId, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            rv.setPendingIntentTemplate(R.id.scores_list, pending);
            appWidgetManager.updateAppWidget(appWidgetId, rv);

        }
        else Log.d(LOG_TAG, "No Day Scores configuration found for widgetId=" + Integer.toString(appWidgetId));

    }

}
