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

package com.lithidsw.wallbox.app.wallsnap;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.app.wallsnap.adapters.WallpaperAdapter;
import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.CustomDialogs;

public class WallpaperFragment extends Fragment {
    WallpaperAdapter wallAdapter;
    SharedPreferences prefs;

    private WallpaperLoader wallLoader;

    private FragmentActivity fa;

    GridView wallGrid;
    TextView mTextView;

    ArrayList<String[]> aWall = new ArrayList<String[]>();

    private boolean simpleFirstRun = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RelativeLayout rl = (RelativeLayout) inflater.inflate(R.layout.wallpaper_frag, container, false);
        if (rl != null) {
            fa = super.getActivity();
            wallGrid = (GridView) rl.findViewById(R.id.grid_view);
            prefs = fa.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);

            if (!prefs.getBoolean(C.PREF_WALLPAPER_CHECK_SAVE_SORT, false)) {
                prefs.edit().putInt(C.PREF_WALLPAPER_CURRENT_SORT, 0).commit();
            }

            wallGrid = (GridView) rl.findViewById(R.id.grid_view);
            mTextView = (TextView) rl.findViewById(R.id.no_content);
            wallAdapter = new WallpaperAdapter(fa, aWall);
            wallGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	Intent intent = new Intent(fa, WallSnapActivity.class);
                	Bundle bundle = new Bundle();
                	bundle.putString(C.EXTRA_PATH, aWall.get(position)[0]);
                	intent.putExtras(bundle);
                	startActivity(intent);
                }
            });
            wallGrid.setAdapter(wallAdapter);
            setHasOptionsMenu(true);

            if (!prefs.getBoolean(C.PREF_WALLPAPER_FIRST_RUN_MAIN, false)) {
                prefs.edit().putBoolean(C.PREF_WALLPAPER_FIRST_RUN_MAIN, true).commit();
                String title = getResources().getString(R.string.main_title_wallpapers);
                String message = getResources().getString(R.string.wallpaper_description);
                new CustomDialogs().openFirstDialog(fa, title, message);
            }
        }
        return rl;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu.size() > 0) {
            if (!simpleFirstRun) {
                if (!prefs.getBoolean(C.PREF_WALLPAPER_CHECK_SAVE_SORT, false)) {
                    prefs.edit().putInt(C.PREF_WALLPAPER_CURRENT_SORT, 0);
                }
                if (!prefs.getBoolean(C.PREF_WALLPAPER_FIRST_RUN_MAIN, false)) {
                    runReload();
                    prefs.edit().putBoolean(C.PREF_WALLPAPER_FIRST_RUN_MAIN, true).commit();
                } else {
                    runReload();
                }
                simpleFirstRun = true;
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                runReload();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        runReload();
    }

    private void runReload() {
        stopWallLoader();
        wallLoader = (WallpaperLoader) new WallpaperLoader().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopWallLoader();
    }
    
    private void stopWallLoader() {
        if (wallLoader != null
                && wallLoader.getStatus() != WallpaperLoader.Status.FINISHED) {
            wallLoader.cancel(true);
            wallLoader = null;
        }
    }

    class WallpaperLoader extends AsyncTask<String, String, ArrayList<String[]>> {

        @Override
        protected void onPreExecute() {
            fa.setProgressBarIndeterminateVisibility(true);
            aWall.clear();
            wallGrid.invalidateViews();
        }

        @Override
        protected ArrayList<String[]> doInBackground(String... arg) {
        	
            File storageDir = new File(
                    Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES
                    ),
                    "WallSnap"
            );
            
            ArrayList<String[]> list = new ArrayList<String[]>();
            
            if (storageDir.exists()) {
                for (File file : storageDir.listFiles()) {
                	if (!file.isDirectory()) {
                    	String[] f = new String[2];
                    	f[0] = file.getAbsolutePath();
                    	f[1] = 	file.getName();
                    	list.add(f);
                	}
                }	
            }
            
            return list;
        }

        @Override
        protected void onPostExecute(ArrayList<String[]> list) {
            fa.setProgressBarIndeterminateVisibility(false);
            aWall.addAll(list);
            wallAdapter.notifyDataSetChanged();
            if (list.size() > 0) {
                wallGrid.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
            } else {
                wallGrid.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
            }
        }
    }
}
