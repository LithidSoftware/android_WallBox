package com.lithidsw.wallbox.app.theme;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.utils.C;

public class ThemesMainFragment extends Fragment {

    SharedPreferences prefs;
    FragmentActivity mActivity;
    View mLayout;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    private int mChoosenTheme = -1;
    private static final int LIGHT_DARKACTIONBAR_THEME = 0;
    private static final int DARK_THEME = 1;
    private static final int LIGHT_THEME = 2;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = super.getActivity();
        prefs = mActivity.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
        mLayout = inflater.inflate(R.layout.themes_main_frag, container, false);
        if (mLayout != null) {
            prefs = mActivity.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
            mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
            mViewPager = (ViewPager) mLayout.findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(0);
            setHasOptionsMenu(true);
        }
        return mLayout;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_apply:
                mChoosenTheme = mViewPager.getCurrentItem();
                if (mChoosenTheme >= 0) {
                    onApplyClicked();
                }
                return true;
            default:
                return false;
        }
    }

    public void onApplyClicked() {
        Intent intent = mActivity.getIntent();
        intent.putExtra("open_themes_drawer", true);
        switch (mChoosenTheme) {
            case DARK_THEME:
                prefs.edit().putString(C.PREF_THEME, "Dark").commit();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mActivity.finish();
                startActivity(intent);
                break;
            case LIGHT_THEME:
                prefs.edit().putString(C.PREF_THEME, "Light").commit();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mActivity.finish();
                startActivity(intent);
                break;
            case LIGHT_DARKACTIONBAR_THEME:
                prefs.edit().putString(C.PREF_THEME, "LightDarkActionBar").commit();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                mActivity.finish();
                startActivity(intent);
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new ThemeFragment();
            Bundle bundle = new Bundle();
            int[] theme = new int[2];
            switch (position) {
                case 0:
                    theme[0] = R.drawable.ic_preview_lightdarkactionbar;
                    theme[1] = R.drawable.ic_preview_lightdarkactionbar_land;
                    bundle.putIntArray("theme_preview", theme);
                    break;
                case 1:
                    theme[0] = R.drawable.ic_preview_dark;
                    theme[1] = R.drawable.ic_preview_dark_land;
                    bundle.putIntArray("theme_preview", theme);
                    break;
                case 2:
                    theme[0] = R.drawable.ic_preview_light;
                    theme[1] = R.drawable.ic_preview_light_land;
                    bundle.putIntArray("theme_preview", theme);
                    break;
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "LightDarkActionBar";
                case 1:
                    return "Dark";
                case 2:
                    return "Light";
            }
            return null;
        }
    }
}
