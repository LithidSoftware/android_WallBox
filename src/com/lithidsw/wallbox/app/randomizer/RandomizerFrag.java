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

package com.lithidsw.wallbox.app.randomizer;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.app.randomizer.adapter.RandomizerGridAdapter;
import com.lithidsw.wallbox.app.randomizer.db.TableHelper;
import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.CustomDialogs;
import com.lithidsw.wallbox.utils.MenuHelper;
import com.lithidsw.wallbox.utils.Utils;

public class RandomizerFrag extends Fragment {

    LinearLayout ll;
    FragmentActivity fa;

    SharedPreferences prefs;

    GridView mGridView;
    TextView mTextView;
    ProgressBar mProgressBar;
    RandomizerGridAdapter adapter;
    Utils mUtils;
    GalLoader loader = null;
    MenuHelper menuHelper;

    ArrayList<String[]> mGalItems = new ArrayList<String[]>();
    ArrayList<Integer> mRemoveList = new ArrayList<Integer>();

    private static final int GET_IMAGE_CODE = 8639;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fa = super.getActivity();
        mUtils = new Utils(fa);
        menuHelper = new MenuHelper(fa);
        ll = (LinearLayout) inflater.inflate(R.layout.randomizer_frag, container, false);
        prefs = fa.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);

        adapter = new RandomizerGridAdapter(fa, mGalItems);
        mGridView = (GridView) ll.findViewById(R.id.grid_view);
        mTextView = (TextView) ll.findViewById(R.id.no_content);
        mProgressBar = (ProgressBar) ll.findViewById(R.id.progress);
        mGridView.setAdapter(adapter);

        mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        mGridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {

            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.setTitle("Choose threads");
                mRemoveList.clear();
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.randomizer_selection_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                final SparseBooleanArray checked = mGridView.getCheckedItemPositions();
                switch (menuItem.getItemId()) {
                    case R.id.action_delete:
                        int size = checked.size();
                        for (int i = 0; i < size; i++) {
                            int key = checked.keyAt(i);
                            boolean value = checked.get(key);
                            if (value) {
                                new TableHelper(fa).deleteSinglePaper(mGalItems.get(key)[1]);
                            }
                        }
                        actionMode.finish();
                        new GalLoader().execute();
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {

            }
        });
        setHasOptionsMenu(true);

        if (!prefs.getBoolean(C.PREF_RANDOMIZER_FIRST_RUN_MAIN, false)) {
            prefs.edit().putBoolean(C.PREF_RANDOMIZER_FIRST_RUN_MAIN, true).commit();
            String title = getResources().getString(R.string.main_title_randomize);
            String message = getResources().getString(R.string.randomizer_description);
            new CustomDialogs().openFirstDialog(fa, title, message);
        }
        return ll;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUp();
    }

    private void loadUp() {
        stopLoader();
        loader = (GalLoader) new GalLoader().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent in = new Intent();
                in.setType("image/*");
                in.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(in, "Select Image"), GET_IMAGE_CODE);
                return true;
            case R.id.action_interval:
                if (mGalItems.size() > 1) {
                    singleDialog();
                } else {
                    mUtils.sendToast("Need at least 2 wallpapers to start!");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showU(Integer item) {
        switch (item) {
            case 0:
                mProgressBar.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.GONE);
                mGridView.setVisibility(View.GONE);
                break;
            case 1:
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.GONE);
                new Utils(fa).stopRandomizerAlarms();
                prefs.edit().putInt(C.PREF_RANDOMIZER_INTERVAL, 0).commit();
                break;
            case 2:
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
                mGridView.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void singleDialog() {
        final String title = "Wallpaper update interval";

        CharSequence[] meh = fa.getResources().getStringArray(R.array.time_intervals);
        AlertDialog.Builder builder = new AlertDialog.Builder(fa);
        int selected = prefs.getInt(C.PREF_RANDOMIZER_INTERVAL, 0);
        builder.setTitle(title);
        builder.setSingleChoiceItems(meh, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                prefs.edit().putInt(C.PREF_RANDOMIZER_INTERVAL, selectedPosition).commit();
                new Utils(fa).stopRandomizerAlarms();
                if (selectedPosition > 0) {
                    new Utils(fa).setRandomizerAlarms(selectedPosition);
                    String itemChosen = getResources().getStringArray(R.array.time_intervals)[selectedPosition];
                    Toast.makeText(fa, "Wallpapers set: " + itemChosen, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(fa, "Stopping PapersRand now", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Sike
            }
        });
        builder.show();
    }

    private String checkImage(Uri uri) {
        String image_path = null;
        Cursor cursor = fa.getContentResolver().query(uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            image_path = cursor.getString(0);
            cursor.close();
        }
        return image_path;
    }

    private void galleryAddPic(Uri data) {
        String image = checkImage(data);
        if (image != null) {
            if (!new TableHelper(fa).isExist(Utils.calculateMD5(image))) {
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(image);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                fa.sendBroadcast(mediaScanIntent);
                new TableHelper(fa).writePaperItem(image, Utils.calculateMD5(image));
            } else {
                Toast.makeText(fa, "Picture already exists!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(fa, "Picture returned null, couldn't add this image, try again.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case GET_IMAGE_CODE:
                    galleryAddPic(data.getData());
                    break;
            }
        }
    }

    private void stopLoader() {
        if (loader != null
                && loader.getStatus() != GalLoader.Status.FINISHED) {
            loader.cancel(true);
            loader = null;
        }
    }

    class GalLoader extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {
            showU(0);
            mGalItems.clear();
            mGridView.invalidateViews();
        }

        @Override
        protected Integer doInBackground(String... strings) {
            ArrayList<String[]> list = new TableHelper(fa).getAllPapers();
            if (list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    String[] item = new String[2];
                    item[0] = list.get(i)[0];
                    item[1] = list.get(i)[1];
                    mGalItems.add(item);
                }
                return mGalItems.size();
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer count) {
            adapter.notifyDataSetChanged();
            if (count > 0) {
                showU(2);
            } else {
                showU(1);
            }
        }
    }
}
