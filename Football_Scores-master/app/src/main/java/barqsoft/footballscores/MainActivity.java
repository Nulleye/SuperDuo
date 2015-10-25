package barqsoft.footballscores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.sync.ScoresSyncAdapter;

public class MainActivity extends ActionBarActivity
{

    public static int selected_match_id;
    public static int current_fragment = 2;
    public static String LOG_TAG = MainActivity.class.getSimpleName();
    private final String save_tag = "Save Test";

    private PagerFragment my_main;

    public PagerFragment getMyMain() { return my_main; }

    protected String getTag() {
        return LOG_TAG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getTag(), "onCreate()");
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            my_main = new PagerFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, my_main)
                    .commit();
        }
        ScoresSyncAdapter.initializeSyncAdapter(this);

        //TODO remove FORCE SYNC on app launch
        if (savedInstanceState == null)
            ScoresSyncAdapter.syncImmediately(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about)
        {
            Intent start_about = new Intent(this,AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        Log.v(save_tag,"will save");
        Log.v(save_tag,"fragment: "+String.valueOf(my_main.mPagerHandler.getCurrentItem()));
        Log.v(save_tag,"selected id: "+selected_match_id);
        outState.putInt("Pager_Current",my_main.mPagerHandler.getCurrentItem());
        outState.putInt("Selected_match",selected_match_id);
        getSupportFragmentManager().putFragment(outState,"my_main",my_main);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.v(save_tag,"will retrive");
        Log.v(save_tag,"fragment: "+String.valueOf(savedInstanceState.getInt("Pager_Current")));
        Log.v(save_tag,"selected id: "+savedInstanceState.getInt("Selected_match"));
        current_fragment = savedInstanceState.getInt("Pager_Current");
        selected_match_id = savedInstanceState.getInt("Selected_match");
        my_main = (PagerFragment) getSupportFragmentManager().getFragment(savedInstanceState,"my_main");
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Intent intent = getIntent();
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                Utilies.WidgetConfiguration config = Utilies.getWidgetConfiguration(intent);
                if (config != null) {
                    if (config.scoreId != null) selected_match_id = config.scoreId;
                    my_main.selectConfiguration(config);
                }
            }
        }
    }

}
