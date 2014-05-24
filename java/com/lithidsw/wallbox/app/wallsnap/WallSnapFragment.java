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

import java.util.Random;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.utils.C;
import com.lithidsw.wallbox.utils.ColorFilterGenerator;
import com.lithidsw.wallbox.utils.CustomDialogs;
import com.lithidsw.wallbox.utils.Utils;

public class WallSnapFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    FragmentActivity mActivity;
    Resources mResources;
    LinearLayout mLayout;
    LinearLayout mLayoutMainClick;
    SharedPreferences prefs;
    Utils mUtils;
    SeekBar mSeekBlur;
    SeekBar mSeekSat;
    SeekBar mSeekHue;
    SeekBar mSeekBright;
    SeekBar mSeekContrast;
    ImageView mImageView;
    Bitmap mBitmap;
    int mBlurItem;
    int mSatItem;
    int mHueItem;
    int mBrightItem;
    int mContrastItem;
    int mSelectedItem;
    private WallpaperManager wm;
    private Bundle bundle;
    private Drawable bundleDrawable;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = super.getActivity();
        mResources = mActivity.getResources();
        mLayout = (LinearLayout) inflater.inflate(R.layout.wallsnap_frag, container, false);
        if (mLayout != null ) {
        	bundle = getArguments();
            wm = WallpaperManager.getInstance(mActivity);
            mUtils = new Utils(mActivity);
            prefs = mActivity.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
            mSeekBlur = (SeekBar) mLayout.findViewById(R.id.blur_seeker);
            mSeekBlur.setOnSeekBarChangeListener(this);
            mSeekSat = (SeekBar) mLayout.findViewById(R.id.sat_seeker);
            mSeekSat.setOnSeekBarChangeListener(this);
            mSeekHue = (SeekBar) mLayout.findViewById(R.id.hue_seeker);
            mSeekHue.setOnSeekBarChangeListener(this);
            mSeekBright = (SeekBar) mLayout.findViewById(R.id.bright_seeker);
            mSeekBright.setOnSeekBarChangeListener(this);
            mSeekContrast = (SeekBar) mLayout.findViewById(R.id.contrast_seeker);
            mSeekContrast.setOnSeekBarChangeListener(this);
            mLayoutMainClick = (LinearLayout) mLayout.findViewById(R.id.main_layout);
            mLayoutMainClick.setOnClickListener(this);
            mImageView = (ImageView) mLayout.findViewById(R.id.main_image);
            mImageView.setImageDrawable(getMainDrawable());
            setSeekers();
        }

        if (!prefs.getBoolean(C.PREF_WALLSNAP_FIRST_RUN_MAIN, false)) {
            prefs.edit().putBoolean(C.PREF_WALLSNAP_FIRST_RUN_MAIN, true).commit();
            String title = mResources.getString(R.string.main_title_wallsnap);
            String message = mResources.getString(R.string.wallsnap_description);
            new CustomDialogs().openFirstDialog(mActivity, title, message);
        }
        setHasOptionsMenu(true);
        return mLayout;
    }
    
    private Drawable getMainDrawable() {
    	Drawable drawable = null;
    	if (bundle != null) {
    		if (bundleDrawable == null) {
    			bundleDrawable = Drawable.createFromPath(bundle.getString(C.EXTRA_PATH));
        		if (bundleDrawable != null) {
        			drawable = bundleDrawable;
        		}	
    		} else {
    			drawable = bundleDrawable;
    		}
    	}
    	
    	if (drawable == null) {
    		drawable = wm.getDrawable();
    	}
    	return drawable;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                sortDialog();
                return true;
            case R.id.action_refresh:
                resetColors();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        check();
    }

    private void setSeekers() {
        mSeekBlur.setMax(24);
        mSeekSat.setMax(200);
        mSeekSat.setProgress(100);
        mSeekHue.setMax(320);
        mSeekHue.setProgress(180);
        mSeekBright.setMax(200);
        mSeekBright.setProgress(100);
        mSeekContrast.setMax(100);
    }

    private void randomImage() {
        resetColors();
        Random r = new Random();
        mSatItem = r.nextInt(200) + 1;
        mSeekSat.setProgress(mSatItem);
        mHueItem = r.nextInt(320) + 1;
        mSeekHue.setProgress(mHueItem);
        mBrightItem = r.nextInt(100) + 1;
        mSeekBright.setProgress(mBrightItem);
        mContrastItem = r.nextInt(100) + 1;
        mSeekContrast.setProgress(mContrastItem);
        updateImagePre();
    }

    private void updateImagePre() {
        Drawable drawable = mImageView.getDrawable();
        if (drawable != null) {
            drawable.setColorFilter(ColorFilterGenerator.adjustColor(mBrightItem, mContrastItem, mSatItem, mHueItem));
        }
    }

    Bitmap BlurImage (Bitmap input, int radius) {
        RenderScript rsScript = RenderScript.create(mActivity);
        Allocation alloc = Allocation.createFromBitmap(rsScript, input);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, alloc.getElement());
        blur.setRadius(radius + 1);
        blur.setInput(alloc);
        Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig());
        Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);
        blur.forEach(outAlloc);
        outAlloc.copyTo(result);
        rsScript.destroy();
        return result;
    }

    private void updateBlur() {
        Bitmap bitmap = BlurImage(Utils.drawableToBitmap(getMainDrawable()), mBlurItem);
        if (bitmap != null) {
            mImageView.setImageBitmap(BlurImage(bitmap, mBlurItem));
            Drawable drawable = mImageView.getDrawable();
            if (drawable != null) {
                drawable.setColorFilter(ColorFilterGenerator.adjustColor(mBrightItem, mContrastItem, mSatItem, mHueItem));
            }
        }
    }

    private void toast4Live() {
    	if (bundle == null) {
            try {
                WallpaperInfo info = wm.getWallpaperInfo();
                if (info != null) {
                    info.toString();
                    mUtils.sendToast(mResources.getString(R.string.last_known_current_live));
                }
            } catch (NullPointerException ignore) {
            }	
    	}
    }

    private void resetColors() {
        mSeekSat.setProgress(100);
        mSeekHue.setProgress(180);
        mSeekBright.setProgress(100);
        mSeekContrast.setProgress(0);
        check();
    }

    public void check() {
        toast4Live();
        mImageView.setImageDrawable(getMainDrawable());
    }

    private void sortDialog() {
        String[] items = mResources.getStringArray(R.array.wallsnap_types);
        new AlertDialog.Builder(mActivity)
                .setTitle(mResources.getString(R.string.wallsnap_type_dialog_title))
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new ImageSaver().execute(which);
                    }
                }).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_layout:
                randomImage();
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.blur_seeker:
                mBlurItem = progress;
                break;
            case R.id.sat_seeker:
                mSatItem = (progress - 100);
                break;
            case R.id.hue_seeker:
                mHueItem = (progress - 180);
                break;
            case R.id.bright_seeker:
                mBrightItem = (progress - 100);
                break;
            case R.id.contrast_seeker:
                mContrastItem = progress;
                break;
        }
        updateImagePre();
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch (seekBar.getId()) {
            case R.id.blur_seeker:
                updateBlur();
                break;
        }
    }

    class ImageSaver extends AsyncTask<Integer, String, Boolean> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(mActivity, "", mResources.getStringArray(R.array.wallsnap_types_progress)[mSelectedItem]);
        }

        @Override
        protected Boolean doInBackground(Integer... type) {
            boolean success;
            Bitmap bitmap = BlurImage(Utils.drawableToBitmap(getMainDrawable()), mBlurItem);
            mBitmap = mUtils.toColor(bitmap, mBrightItem, mContrastItem, mSatItem, mHueItem);
            switch (type[0]) {
                case 0:
                    success = mUtils.saveWallSnap(mBitmap);
                    break;
                case 1:
                    success = mUtils.saveWallSnap(mBitmap);
                    if (success) {
                        success = mUtils.setWallpaperBitmap(mBitmap);
                    }
                    break;
                case 2:
                    success = mUtils.setWallpaperBitmap(mBitmap);
                    break;
                default:
                    success = false;
                    break;
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean count) {
            progressDialog.dismiss();
            if (count) {
                mUtils.sendToast(mResources.getStringArray(R.array.wallsnap_types_toast)[mSelectedItem]);
                switch (mSelectedItem) {
                    case 0:
                        resetColors();
                        break;
                    default:
                        mImageView.setImageBitmap(mBitmap);
                        resetColors();
                        break;
                }
            } else {
                mUtils.sendToast(mResources.getString(R.string.wallpaper_error));
            }

            check();
        }
    }
}
