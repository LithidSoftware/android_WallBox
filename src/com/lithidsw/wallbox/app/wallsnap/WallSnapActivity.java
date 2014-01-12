package com.lithidsw.wallbox.app.wallsnap;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.utils.C;

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class WallSnapActivity extends FragmentActivity {
	
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(C.PREF, MODE_PRIVATE);
        String themeSty = prefs.getString(C.PREF_THEME, null);
        if (themeSty != null) {
            setTheme(getResources().getIdentifier(themeSty, "style", C.THIS));
        } else {
            setTheme(C.DEFAULT_THEME);
        }
		FrameLayout fl = new FrameLayout(this);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		setContentView(fl, lp);
		
        if (savedInstanceState == null) {
            Fragment newFragment = new WallSnapFragment();
            newFragment.setArguments(getIntent().getExtras());
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(android.R.id.content, newFragment);
            ft.commit();
        }
        
        setupActionBar();
	}
	
    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.wallsnap_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
    }
}
