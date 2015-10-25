package barqsoft.footballscores;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment
{
    public static final int NUM_PAGES = 5;
    public ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[5];

    public static final int WORKING_MODE_NORMAL = 0;
    public static final int WORKING_MODE_CHOOSE_DAY = 1;
    public static final int WORKING_MODE_CHOOSE_SCORE = 2;

    private int mWorkingMode = WORKING_MODE_NORMAL;

    public int getWorkingMode() { return mWorkingMode; }
    public void setWorkingMode(int workingMode) { mWorkingMode = workingMode; }
    boolean rtl = true;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());

        int messageId = -1;
        switch (mWorkingMode) {
            case WORKING_MODE_CHOOSE_SCORE:
                messageId = R.string.widget_message_choose_score;
                break;
            case WORKING_MODE_CHOOSE_DAY:
                messageId = R.string.widget_message_choose_day;
                break;
        }
        if (messageId != -1) {
            TextView message = (TextView) rootView.findViewById(R.id.widget_message);
            if (message != null) {
                message.setVisibility(View.VISIBLE);
                message.setText(messageId);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Configuration config = getResources().getConfiguration();
            rtl = (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
        }
        for (int i = 0;i < NUM_PAGES;i++) {
            viewFragments[i] = new MainScreenFragment();
            if (!rtl)
                viewFragments[i].setFragmentDate(new Date(System.currentTimeMillis()+((i-2)*86400000)));
            else
                viewFragments[i].setFragmentDate(new Date(System.currentTimeMillis() + ((((NUM_PAGES-1)-(i+2)) * 86400000))));
            viewFragments[i].setWorkingMode(mWorkingMode);
        }

        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.current_fragment);
        return rootView;
    }





    private class myPageAdapter extends FragmentStatePagerAdapter
    {
        @Override
        public Fragment getItem(int i) {
            return viewFragments[i];
        }

        @Override
        public int getCount()
        {
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm)
        {
            super(fm);
        }
        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position)
        {
            if (!rtl)
                return Utilies.getDayName(getActivity(),System.currentTimeMillis()+((position-2)*86400000));
            else
                return Utilies.getDayName(getActivity(), System.currentTimeMillis() + (((NUM_PAGES-1)-(position+2)) * 86400000));
        }

    }

    public void selectConfiguration(final Utilies.WidgetConfiguration config) {
        final String date = Utilies.formatDate(config.date);
        for(int i=0;i<viewFragments.length;i++)
            if (date.equals(viewFragments[i].getFragmentDateText())) {
                if (config.scoreId != null)
                    viewFragments[i].setSelectItemId(config.scoreId);
                mPagerHandler.setCurrentItem(i);
            }
    }

}
