package barqsoft.footballscores.widget;

import android.os.Bundle;

import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.PagerFragment;

/**
 * Created by cristian on 19/10/15.
 */
public class ScoreWidgetConfiguration extends MainActivity {

    public static String LOG_TAG = ScoreWidgetConfiguration.class.getSimpleName();

    @Override
    protected String getTag() {
        return LOG_TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PagerFragment fragment = getMyMain();
        if (fragment != null) fragment.setWorkingMode(PagerFragment.WORKING_MODE_CHOOSE_SCORE);
        setResult(RESULT_CANCELED);
    }

}
