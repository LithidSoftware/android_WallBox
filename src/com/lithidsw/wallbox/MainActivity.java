package com.lithidsw.wallbox;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lithidsw.wallbox.app.colorwall.ColorWallFrag;
import com.lithidsw.wallbox.app.randomizer.RandomizerFrag;
import com.lithidsw.wallbox.app.saturate.SaturateFrag;
import com.lithidsw.wallbox.app.theme.ThemesMainFragment;
import com.lithidsw.wallbox.app.wallsnap.WallSnapFragment;
import com.lithidsw.wallbox.app.wallsnap.WallpaperFragment;
import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.MenuHelper;

public class MainActivity extends FragmentActivity {
    SharedPreferences prefs;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mTitle;
    private String[] mMainTitles;

    private ActionBar actionBar;
    private MenuHelper menuHelper;

    private static final int WALLSNAP = 0;
    private static final int WALLPAPER = 1;
    private static final int COLORWALL = 2;
    private static final int SATURATE = 3;
    private static final int RANDOMIZE = 4;
    private static final int THEMES = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        String themeSty = prefs.getString(C.PREF_THEME, null);
        if (themeSty != null) {
            setTheme(getResources().getIdentifier(themeSty, "style", C.THIS));
        } else {
            setTheme(C.DEFAULT_THEME);
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        mTitle = getTitle();
        menuHelper = new MenuHelper(this);
        mMainTitles = getResources().getStringArray(R.array.main_titles);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMainTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout != null) {
            setupActionBar();
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerToggle = new ActionBarDrawerToggle(
                    this,
                    mDrawerLayout,
                    R.drawable.ic_drawer,
                    R.string.drawer_open,
                    R.string.drawer_close
            ) {
                public void onDrawerClosed(View view) {
                    setTitle(mMainTitles[mDrawerList.getCheckedItemPosition()]);
                    invalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                    setTitle(mTitle);
                    setProgressBarIndeterminateVisibility(false);
                    invalidateOptionsMenu();
                }
            };
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }

        if (savedInstanceState == null) {
            if (getIntent().getBooleanExtra("open_themes_drawer", false)) {
                selectItem(5);
            } else {
                selectItem(0);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if (mDrawerLayout == null || !mDrawerLayout.isDrawerOpen(mDrawerList)) {
            MenuInflater inflater = getMenuInflater();
            int item = mDrawerList.getCheckedItemPosition();
            switch (item) {
                case WALLSNAP:
                    inflater.inflate(R.menu.wallsnap_menu, menu);
                    break;
                case COLORWALL:
                    inflater.inflate(R.menu.colorwall_menu, menu);
                    break;
                case SATURATE:
                    inflater.inflate(R.menu.saturate_menu, menu);
                    Log.e("", "Loaded menu item sat");
                    break;
                case RANDOMIZE:
                    inflater.inflate(R.menu.randomizer_menu, menu);
                    break;
                case WALLPAPER:
                    inflater.inflate(R.menu.wallpaper_menu, menu);
                    break;
                case THEMES:
                    inflater.inflate(R.menu.theme_menu, menu);
                    break;
            }
        }

        menuHelper.setAbout(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerLayout != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else {
            switch (item.getItemId()) {
                case R.id.action_about:
                    startActivity(new Intent(MainActivity.this, AboutActivity.class));
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupActionBar() {
        actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new WallSnapFragment();
                break;
            case 1:
            	fragment = new WallpaperFragment();
                break;
            case 2:
            	fragment = new ColorWallFrag();
                break;
            case 3:
            	fragment = new SaturateFrag();
                break;
            case 4:
            	fragment = new RandomizerFrag();
                break;
            case 5:
                fragment = new ThemesMainFragment();
                break;
            default:
                fragment = new ThemesMainFragment();
                break;
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        mDrawerList.setItemChecked(position, true);
        if (mDrawerLayout != null ) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        invalidateOptionsMenu();
    }

    @Override
    public void setTitle(CharSequence title) {
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerLayout != null ) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerLayout != null ) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDrawerLayout != null ) {
            mDrawerLayout.closeDrawer(mDrawerList);
            setTitle(mMainTitles[mDrawerList.getCheckedItemPosition()]);
        }
    }
}
