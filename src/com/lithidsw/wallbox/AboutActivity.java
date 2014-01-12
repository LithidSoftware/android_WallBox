package com.lithidsw.wallbox;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.ContribDialog;
import com.lithidsw.wallbox.utils.Utils;

public class AboutActivity extends Activity implements View.OnClickListener, View.OnLongClickListener{

    SharedPreferences prefs;
    Context mContext;
    private Utils mUtils;
    private ImageButton btn_googleplus;
    private ImageButton btn_contribute;
    private ImageButton btn_changelog;
    private ImageButton btn_donate;

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
        setContentView(R.layout.about_frag);
        setupActionBar();
        mContext = this;
        mUtils = new Utils(mContext);
        btn_googleplus = (ImageButton) findViewById(R.id.btn_googleplus);
        btn_contribute = (ImageButton) findViewById(R.id.btn_contribute);
        btn_changelog = (ImageButton) findViewById(R.id.btn_changelog);
        btn_donate = (ImageButton) findViewById(R.id.btn_donate);
        btn_googleplus.setOnClickListener(this);
        btn_contribute.setOnClickListener(this);
        btn_changelog.setOnClickListener(this);
        btn_donate.setOnClickListener(this);
        btn_googleplus.setOnLongClickListener(this);
        btn_contribute.setOnLongClickListener(this);
        btn_changelog.setOnLongClickListener(this);
        btn_donate.setOnLongClickListener(this);
    }

    private void setupActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_googleplus:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/communities/104381277142730834991")));
                break;
            case R.id.btn_contribute:
                new ContribDialog(mContext).showContribAlert(0);
                break;
            case R.id.btn_changelog:
                new ContribDialog(mContext).showContribAlert(1);
                break;
            case R.id.btn_donate:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://goo.gl/511ca")));
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.btn_googleplus:
                mUtils.sendToast(getString(R.string.googleplus));
                return true;
            case R.id.btn_contribute:
                mUtils.sendToast(getString(R.string.contribute));
                return true;
            case R.id.btn_changelog:
                mUtils.sendToast(getString(R.string.changelog));
                return true;
            case R.id.btn_donate:
                mUtils.sendToast(getString(R.string.donate));
                return true;
        }
        return false;
    }
}
