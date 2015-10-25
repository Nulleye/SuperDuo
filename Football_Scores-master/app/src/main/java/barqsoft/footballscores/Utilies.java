package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilies {

    public static final String BASE_PREFS = "barqsoft.footballscores";
    public static final String WIDGET_TYPE_DAY = "widget_day";
    public static final String WIDGET_TYPE_SCORE = "widget_score";
    public static final String WIDGET_DATE = "widget_date";
    public static final String WIDGET_SCORE_ID = "widget_score_id";
    public static final int WIDGET_NO_VALUE = -1;

    public static class WidgetConfiguration {
        public Date date;
        public Integer scoreId;
        public WidgetConfiguration(final Date date, final Integer scoreId) {
            this.date = date;
            this.scoreId = scoreId;
        }
        public WidgetConfiguration(final Date date) {
            this.date = date;
            this.scoreId =  null;
        }
    }

    public static void writeWidgetConfiguration(final Context context,
            final String widgetType, final int widgetId, final WidgetConfiguration widgetConfig) {
        final SharedPreferences.Editor editor = context.getSharedPreferences(Utilies.BASE_PREFS, Context.MODE_PRIVATE).edit();
        if (widgetConfig.date != null)
            editor.putLong(widgetType + "." + Integer.toString(widgetId) + "." + WIDGET_DATE, widgetConfig.date.getTime());
        if (widgetConfig.scoreId != null)
            editor.putInt(widgetType + "." + Integer.toString(widgetId) + "." + WIDGET_SCORE_ID, widgetConfig.scoreId);
        editor.commit();
    }

    public static WidgetConfiguration readWidgetConfiguration(final Context context,
            final String widgetType, final int widgetId) {
        return readWidgetConfiguration(
                context.getSharedPreferences(Utilies.BASE_PREFS, Context.MODE_PRIVATE), widgetType, widgetId);
    }

    protected static WidgetConfiguration readWidgetConfiguration(final SharedPreferences prefs,
            final String widgetType, final int widgetId) {
        long dateLong = prefs.getLong(widgetType + "." + Integer.toString(widgetId) + "." + WIDGET_DATE, WIDGET_NO_VALUE);
        if (dateLong != WIDGET_NO_VALUE) {
            int scoreId = prefs.getInt(widgetType + "." + Integer.toString(widgetId) + "." + WIDGET_SCORE_ID, WIDGET_NO_VALUE);
            if (scoreId != WIDGET_NO_VALUE) return new WidgetConfiguration(new Date(dateLong), scoreId);
            else return new WidgetConfiguration(new Date(dateLong));
        }
        return null;
    }

    public static void deleteWidgetConfiguration(final Context context,
            final String widgetType, final int widgetId) {
        final SharedPreferences.Editor editor = context.getSharedPreferences(Utilies.BASE_PREFS, Context.MODE_PRIVATE).edit();
        editor.remove(widgetType + "." + Integer.toString(widgetId));
        editor.commit();
    }

    public static void putWidgetConfiguration(Intent intent, WidgetConfiguration widgetConfig) {
        if (widgetConfig.date != null)
            intent.putExtra(WIDGET_DATE, widgetConfig.date.getTime());
        if (widgetConfig.scoreId != null)
            intent.putExtra(WIDGET_SCORE_ID, widgetConfig.scoreId);
    }

    public static WidgetConfiguration getWidgetConfiguration(Intent intent) {
        long dateLong = intent.getLongExtra(WIDGET_DATE, WIDGET_NO_VALUE);
        if (dateLong != WIDGET_NO_VALUE) {
            int scoreId = intent.getIntExtra(WIDGET_SCORE_ID, WIDGET_NO_VALUE);
            if (scoreId != WIDGET_NO_VALUE) return new WidgetConfiguration(new Date(dateLong), scoreId);
            else return new WidgetConfiguration(new Date(dateLong));
        }
        return null;
    }

    public static int[] cleanWidgets(final Context context, final int[] ids, final String widgetType) {
        if ((ids != null) && (ids.length > 0)) {
            SharedPreferences prefs = context.getSharedPreferences(Utilies.BASE_PREFS, Context.MODE_PRIVATE);
            List<Integer> resultList = new ArrayList<Integer>(ids.length);
            for (int i = 0; i < ids.length; i++)
                if (readWidgetConfiguration(prefs, widgetType, ids[i]) != null)
                    resultList.add(ids[i]);
            if (resultList.size() > 0) {
                int[] result = new int[resultList.size()];
                for (int i = 0; i < result.length; i++)
                    result[i] = resultList.get(i);
                return result;
            }
        }
        return null;
    }

    private static SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
    public static String formatDate(final Date date) {
        return mformat.format(date);
    }

    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        }
        else if ( julianDay == currentJulianDay -1)
        {
            return context.getString(R.string.yesterday);
        }
        else
        {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    public static final int SERIE_A = 357;
    public static final int PREMIER_LEGAUE = 354;
    public static final int CHAMPIONS_LEAGUE = 362;
    public static final int PRIMERA_DIVISION = 358;
    public static final int BUNDESLIGA = 351;
    public static String getLeague(int league_num)
    {
        switch (league_num)
        {
            case SERIE_A : return "Seria A";
            case PREMIER_LEGAUE : return "Premier League";
            case CHAMPIONS_LEAGUE : return "UEFA Champions League";
            case PRIMERA_DIVISION : return "Primera Division";
            case BUNDESLIGA : return "Bundesliga";
            default: return "Not known League Please report";
        }
    }
    public static String getMatchDay(int match_day,int league_num)
    {
        if(league_num == CHAMPIONS_LEAGUE)
        {
            if (match_day <= 6)
            {
                return "Group Stages, Matchday : 6";
            }
            else if(match_day == 7 || match_day == 8)
            {
                return "First Knockout round";
            }
            else if(match_day == 9 || match_day == 10)
            {
                return "QuarterFinal";
            }
            else if(match_day == 11 || match_day == 12)
            {
                return "SemiFinal";
            }
            else
            {
                return "Final";
            }
        }
        else
        {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals,int awaygoals)
    {
        if(home_goals < 0 || awaygoals < 0)
        {
            return " - ";
        }
        else
        {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    protected static final Pattern TEAMS_TOKENS =
            Pattern.compile(
                    "(?i)(" +
                    "arsenal|manchester\\s+united|swansea|leicester|" +
                    "everton|west\\s+ham\\s+united|tottenham|west\\s+bromwich|" +
                    "sunderland|stoke" +
                    ")"
            );

    public static int getTeamCrestByTeamName (String teamname)
    {
//TODO: descriptions seems to have changed, so we get a more permisive approach, however
//      we should request the Crest to the Football API and store it on a database table
//        if (teamname==null) return R.drawable.no_icon;
//        switch (teamname)
//        { //This is the set of icons that are currently in the app. Feel free to find and add more
//            //as you go.
//            case "Arsenal London FC" : return R.drawable.arsenal;
//            case "Manchester United FC" : return R.drawable.manchester_united;
//            case "Swansea City" : return R.drawable.swansea_city_afc;
//            case "Leicester City" : return R.drawable.leicester_city_fc_hd_logo;
//            case "Everton FC" : return R.drawable.everton_fc_logo1;
//            case "West Ham United FC" : return R.drawable.west_ham;
//            case "Tottenham Hotspur FC" : return R.drawable.tottenham_hotspur;
//            case "West Bromwich Albion" : return R.drawable.west_bromwich_albion_hd_logo;
//            case "Sunderland AFC" : return R.drawable.sunderland;
//            case "Stoke City FC" : return R.drawable.stoke_city;
//            default: return R.drawable.no_icon;
//        }

        if (teamname == null) return R.drawable.no_icon;
        Matcher matcher = TEAMS_TOKENS.matcher(teamname);
        if (matcher.find()) {
            switch (matcher.group(1).replaceAll("\\s+", " ").toLowerCase()) {
                case "arsenal":
                    return R.drawable.arsenal;
                case "manchester united":
                    return R.drawable.manchester_united;
                case "swansea":
                    return R.drawable.swansea_city_afc;
                case "leicester":
                    return R.drawable.leicester_city_fc_hd_logo;
                case "everton":
                    return R.drawable.everton_fc_logo1;
                case "west ham":
                    return R.drawable.west_ham;
                case "tottenham":
                    return R.drawable.tottenham_hotspur;
                case "west bromwich":
                    return R.drawable.west_bromwich_albion_hd_logo;
                case "sunderland":
                    return R.drawable.sunderland;
                case "stoke":
                    return R.drawable.stoke_city;
                default:
                    return R.drawable.no_icon;
            }
        }
        return R.drawable.no_icon;
    }
}
