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

package com.lithidsw.wallbox.app.colorwall;

import java.io.IOException;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.CustomDialogs;

public class ColorWallFrag extends Fragment implements View.OnClickListener {

    LinearLayout ll;
    FragmentActivity fa;

    SharedPreferences prefs;

    private ImageView mPicker;
    private EditText mColorCode;

    private WallpaperManager wm;
    private int mColor;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fa = super.getActivity();
        prefs = fa.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
        ll = (LinearLayout) inflater.inflate(R.layout.colorwall_frag, container, false);
        mColorCode = (EditText) ll.findViewById(R.id.color_code);
        mColorCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 7) {
                    if (s.toString().startsWith("#")) {
                        try {
                            mColor = Color.parseColor(s.toString());
                            mPicker.setImageDrawable(new ColorDrawable(mColor));
                        } catch (NumberFormatException e){
                            mColorCode.setText("");
                            Toast.makeText(fa, "Invalid color format: "+s.toString()+"\nPlease use a valid 6 digit color code", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mColorCode.setText("");
                        Toast.makeText(fa, "Needs to start with # symbol before color code", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        wm = WallpaperManager.getInstance(fa);
        ll.findViewById(R.id.color_black).setOnClickListener(this);
        ll.findViewById(R.id.color_white).setOnClickListener(this);
        ll.findViewById(R.id.color_blue).setOnClickListener(this);
        ll.findViewById(R.id.color_green).setOnClickListener(this);
        ll.findViewById(R.id.color_purple).setOnClickListener(this);
        ll.findViewById(R.id.color_yellow).setOnClickListener(this);
        ll.findViewById(R.id.color_red).setOnClickListener(this);

        mPicker = (ImageView) ll.findViewById(R.id.picker);

        if (!prefs.getBoolean(C.PREF_COLORWALL_FIRST_RUN_MAIN, false)) {
            prefs.edit().putBoolean(C.PREF_COLORWALL_FIRST_RUN_MAIN, true).commit();
            String title = getResources().getString(R.string.main_title_colorwall);
            String message = getResources().getString(R.string.colorwall_description);
            new CustomDialogs().openFirstDialog(fa, title, message);
        }
        setHasOptionsMenu(true);
        return ll;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_apply:
                try {
                    Bitmap bit = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas(bit);
                    can.drawColor(mColor);
                    wm.setBitmap(bit);
                    Toast.makeText(fa, "Current color wallpaper applied!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(fa, "Current color wallpaper had an error!", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.color_black:
                mColor = getResources().getColor(R.color.black);
                mColorCode.setText(R.color.black);
                break;
            case R.id.color_white:
                mColor = getResources().getColor(R.color.white);
                mColorCode.setText(R.color.white);
                break;
            case R.id.color_blue:
                mColor = getResources().getColor(R.color.blue);
                mColorCode.setText(R.color.blue);
                break;
            case R.id.color_purple:
                mColor = getResources().getColor(R.color.purple);
                mColorCode.setText(R.color.purple);
                break;
            case R.id.color_green:
                mColor = getResources().getColor(R.color.green);
                mColorCode.setText(R.color.green);
                break;
            case R.id.color_yellow:
                mColor = getResources().getColor(R.color.yellow);
                mColorCode.setText(R.color.yellow);
                break;
            case R.id.color_red:
                mColor = getResources().getColor(R.color.red);
                mColorCode.setText(R.color.red);
                break;
        }

        mPicker.setImageDrawable(new ColorDrawable(mColor));
    }
}
