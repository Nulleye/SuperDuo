package barqsoft.footballscores;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.Date;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.widget.DayWidget;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener
{
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;

    private Date fragmentDate = new Date();
    private String[] fragmentDateText = new String[1];

    private int last_selected_item = -1;

    private int mWorkingMode = PagerFragment.WORKING_MODE_NORMAL;

    public int getWorkingMode() { return mWorkingMode; }
    public void setWorkingMode(int workingMode) { mWorkingMode = workingMode; }

    private ListView score_list;
    private int selectItemId = -1;

    public MainScreenFragment()
    {
    }

    public void setFragmentDate(Date date)
    {
        fragmentDate = date;
        fragmentDateText[0] = Utilies.formatDate(date);
    }

    public String getFragmentDateText() {
        return fragmentDateText[0];
    }

    public void setSelectItemId(final int selectItemId) {
        this.selectItemId = selectItemId;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(),null,0);
        mAdapter.setDate(fragmentDate);

        switch (mWorkingMode) {
            case PagerFragment.WORKING_MODE_CHOOSE_DAY:
                Button btn = (Button) rootView.findViewById(R.id.widget_select);
                if (btn != null) {
                    btn.setVisibility(View.VISIBLE);
                    btn.setOnClickListener(this);
                }
                break;
            case PagerFragment.WORKING_MODE_CHOOSE_SCORE:
                mAdapter.setWidgetMode(true);
                break;
        }

        score_list.setAdapter(mAdapter);

        mAdapter.detail_match_id = MainActivity.selected_match_id;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                scoresAdapter.ViewHolder selected = (scoresAdapter.ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.match_id;
                MainActivity.selected_match_id = (int) selected.match_id;
                mAdapter.notifyDataSetChanged();
            }
        });

        return rootView;
    }


    @Override
    public void onClick(View v) {
        Activity myActivity = getActivity();
        int widgetId = myActivity.getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            Utilies.writeWidgetConfiguration(myActivity, Utilies.WIDGET_TYPE_DAY, widgetId, new Utilies.WidgetConfiguration(fragmentDate));
            DayWidget.updateWidget(myActivity, AppWidgetManager.getInstance(myActivity), widgetId);
            //Put result and finish
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            myActivity.setResult(Activity.RESULT_OK, resultValue);
            myActivity.finish();
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null,null, fragmentDateText,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        int found = -1;
        if (selectItemId != -1) {
            int i = 0;
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if (cursor.getInt(scoresAdapter.COL_ID) == selectItemId) {
                    found = i;
                    break;
                }
                i++;
                cursor.moveToNext();
            }
            selectItemId = -1;
        }

        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();

        if (found != -1) score_list.setSelection(found);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        mAdapter.swapCursor(null);
    }


}
