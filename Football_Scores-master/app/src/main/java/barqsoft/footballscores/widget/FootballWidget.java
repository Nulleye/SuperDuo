package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import barqsoft.footballscores.Utilies;

/**
 * Created by cristian on 24/10/15.
 */
public abstract class FootballWidget extends AppWidgetProvider {

    public static String WIDGET_IDS_KEY = "widget_ids";

    abstract public String getTag();

    abstract public String getType();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(getTag(), "onReceive()");
        if (intent.hasExtra(WIDGET_IDS_KEY))
            this.onUpdate(context, AppWidgetManager.getInstance(context), intent.getExtras().getIntArray(WIDGET_IDS_KEY));
        else super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(getTag(), "onDeleted()");
        super.onDeleted(context, appWidgetIds);
        for(int i=0; i<appWidgetIds.length; i++)
            Utilies.deleteWidgetConfiguration(context, getType(), appWidgetIds[i]);
    }

}
