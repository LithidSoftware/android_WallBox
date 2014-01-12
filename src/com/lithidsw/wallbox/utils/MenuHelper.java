package com.lithidsw.wallbox.utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.Menu;
import android.view.MenuItem;

import com.lithidsw.wallbox.R;

public class MenuHelper {

    Context mContext;
    Resources mRes;

    public MenuHelper(Context context) {
        mContext = context;
        mRes = mContext.getResources();
    }

    public void setAbout(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, R.id.action_about, Menu.NONE, "About");
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    }
}
