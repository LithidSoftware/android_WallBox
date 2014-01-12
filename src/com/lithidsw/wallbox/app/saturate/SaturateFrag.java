/*
 * Copyright 2014 Jeremie Long
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lithidsw.wallbox.app.saturate;

import java.io.File;
import java.util.Calendar;

import android.app.ProgressDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.CustomDialogs;
import com.lithidsw.wallbox.utils.Utils;

public class SaturateFrag extends Fragment {

    SharedPreferences prefs;
    private Utils mUtils;
    private WallpaperManager wm;
    private ImageView image;
    private float seekerFl;
    SeekBar seek;

    Menu mMenu;

    TextView time_text;
    int hour;

    boolean isRunning = true;

    LinearLayout ll;
    FragmentActivity fa;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fa = super.getActivity();
        ll = (LinearLayout) inflater.inflate(R.layout.saturate_frag, container, false);
        prefs = fa.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
        mUtils = new Utils(fa);
        wm = WallpaperManager.getInstance(fa);
        image = (ImageView) ll.findViewById(R.id.main_image);
        try {
            WallpaperInfo info = wm.getWallpaperInfo();
            info.toString();
            mUtils.sendToast(getResources().getString(R.string.live_wallpaper));
        } catch (NullPointerException ignore) {
        }
        time_text = (TextView) ll.findViewById(R.id.time_text);
        seek = (SeekBar) ll.findViewById(R.id.seeker);
        seek.setMax(23);
        Calendar cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        time_text.setText(mUtils.getHour(hour));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar v, int num, boolean isU) {
                if (!isRunning) {
                    time_text.setText(mUtils.getHour(num));
                    seekerFl = mUtils.getFloat(num);
                    Drawable d = mUtils.convertToGrayscale(wm.getDrawable(), seekerFl);
                    image.setImageDrawable(d);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
                // TODO Auto-generated method stub
            }
        });

        if (!prefs.getBoolean(C.PREF_SATURATE_FIRST_RUN_MAIN, false)) {
            prefs.edit().putBoolean(C.PREF_SATURATE_FIRST_RUN_MAIN, true).commit();
            String title = getResources().getString(R.string.main_title_saturate);
            String message = getResources().getString(R.string.saturate_description);
            new CustomDialogs().openFirstDialog(fa, title, message);
        }
        setHasOptionsMenu(true);
        return ll;
    }

    private void updateViews() {
    	Log.e("", "Size: " + mMenu.size());
        isRunning = prefs.getBoolean(C.PREF_SATURATE_START, false);
        seek.setEnabled(!isRunning);
        if (isRunning) {
            final File file = new File(fa.getFilesDir(), C.CACHEIMG);

            Thread thread = new Thread()
            {
                @Override
                public void run() {
                    final Bitmap myBitmap = BitmapFactory.decodeFile(file.toString());
                    fa.runOnUiThread(new Runnable() {
                        public void run() {
                            image.setImageBitmap(mUtils.toGrayscale(myBitmap, mUtils.h2F()));
                            seek.setProgress(hour);
                            if (mMenu.size() > 1) {
                                mMenu.getItem(0).setVisible(false);
                                mMenu.getItem(1).setVisible(true);	
                            }
                        }
                    });
                }
            };

            thread.start();
        } else {
            seek.setProgress(hour);
            if (mMenu.size() > 1) {
                mMenu.getItem(0).setVisible(true);
                mMenu.getItem(1).setVisible(false);	
            }
        }
    }

    private void setWallpaper() {
        if (isRunning) {
            stopRunning();
            updateViews();
        } else {
            startRunning();
        }
    }

    private void startRunning() {
        new WallpaperLoader().execute();
    }

    private void stopRunning() {
        mUtils.stopSaturatedAlarms();
        mUtils.sendToast(SaturateFrag.this.getResources().getString(R.string.stopped_sat));
        prefs.edit().putBoolean(C.PREF_SATURATE_START, false).commit();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        updateViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_apply:
                setWallpaper();
                return true;
            case R.id.action_stop:
                setWallpaper();
                return true;
            default:
                return false;
        }
    }

    class WallpaperLoader extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(fa, "", fa.getResources().getString(R.string.set_wallpaper));
        }

        @Override
        protected String doInBackground(String... strings) {
            mUtils.setSaturatedWallpaper();
            return null;
        }

        @Override
        protected void onPostExecute(String count) {
            progressDialog.dismiss();
            mUtils.stopSaturatedAlarms();
            mUtils.setSaturatedAlarms();
            prefs.edit().putBoolean(C.PREF_SATURATE_START, true).commit();
            mUtils.sendToast(fa.getResources().getString(R.string.started_sat));
            updateViews();
        }
    }

}
