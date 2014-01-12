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

package com.lithidsw.wallbox.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.app.randomizer.db.TableHelper;

public class Utils {

    Context mContext;
    Toast mToast = null;

    public Utils(Context context) {
        mContext = context;
    }

    public String getScreenSize(String smallStr,
                                String normalStr, String largeStr) {
        String downloadFile = null;

        DisplayMetrics displayMetrics = mContext.getResources()
                .getDisplayMetrics();
        float screenWidthDp = displayMetrics.widthPixels
                / displayMetrics.density;

        float metrics = mContext.getResources().getDisplayMetrics().density;

        if (screenWidthDp >= 600) {
            downloadFile = largeStr;
        } else {
            if (metrics >= 2.0) {
                downloadFile = normalStr;
            } else {
                downloadFile = smallStr;
            }
        }
        return downloadFile;
    }

    public Bitmap changeHue( Bitmap source, int number ) {

        double hue = (number / 100);

        int screenWidth = source.getWidth();
        int screenHeight = source.getHeight();

        Bitmap result = Bitmap.createBitmap( screenWidth, screenHeight, source.getConfig() );

        float[] hsv = new float[3];
        for( int x = 0; x < screenWidth; x++ ) {
            for( int y = 0; y < screenHeight; y++ ) {
                int c = source.getPixel( x, y );
                Color.colorToHSV(c, hsv);
                hsv[0] = (float) ((hsv[0] + 360 * hue) % 360);
                c = (Color.HSVToColor( hsv ) & 0x00ffffff) | (c & 0xff000000);
                result.setPixel( x, y, c );
            }
        }

        return result;
    }

    public int sortPref(SharedPreferences prefs, String item) {
        return prefs.getInt(item, 0);
    }

    public String setRandomizerWallpaperFromFile() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(mContext);

            String[] items = new TableHelper(mContext).getRandPaper();

            if (items == null) {
                return null;
            }

            File file = new File(items[0]);
            Bitmap mBitmap = BitmapFactory.decodeFile(file.toString());
            wm.setBitmap(mBitmap);
            return items[1];
        } catch (IOException e) {
            return null;
        }
    }

    public static String calculateMD5(String itempath) {
        File updateFile = new File(itempath);
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
    }

    public Boolean setWallpaperBitmap(Bitmap bitmap) {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            wm.setBitmap(bitmap);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void setSaturatedWallpaper() {
        try {
            WallpaperManager wm = WallpaperManager.getInstance(mContext);
            Drawable draw = wm.getDrawable();
            saveSaturateImage(drawableToBitmap(draw));
            Bitmap bit = toGrayscale(drawableToBitmap(draw), h2F());
            wm.setBitmap(bit);
        } catch (IOException e) {
            sendToast(mContext.getResources().getString(R.string.apply_error));
        }
    }

    public void setSaturatedWallpaperFromFile(Boolean isRand) {
        WallpaperManager wm = WallpaperManager.getInstance(mContext);
        Bitmap bitmap;
        try {

            if (!isRand) {
                File file = new File(mContext.getFilesDir(), C.CACHEIMG);
                Bitmap myBitmap = BitmapFactory.decodeFile(file.toString());
                bitmap = toGrayscale(myBitmap, h2F());
            } else {
                Drawable draw = wm.getDrawable();
                saveSaturateImage(drawableToBitmap(draw));
                bitmap = toGrayscale(drawableToBitmap(draw), h2F());
            }

            wm.setBitmap(bitmap);
            Log.e(C.TAG, mContext.getResources().getString(R.string.set_wallpaper));
        } catch (IOException e) {
            Log.e(C.TAG, mContext.getResources().getString(R.string.apply_error));
        }
    }

    public void sendToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(mContext, str, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public Drawable convertToGrayscale(Drawable drawable, float sat) {
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(sat);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
        drawable.setColorFilter(filter);
        return drawable;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal, float sat) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(sat);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap toColor(Bitmap bmpOriginal, int brightness, int contrast, int sat, int hue) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        paint.setColorFilter(ColorFilterGenerator.adjustColor(brightness, contrast, sat, hue));
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public boolean saveWallSnap(Bitmap bitmap) {
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                ),
                "WallSnap"
        );

        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        String JPEG_FILE_PREFIX = "WallSnap";
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp;
        try {
            File image = File.createTempFile(imageFileName, ".png", storageDir);
            FileOutputStream out = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
            out.flush();
            out.close();

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(Uri.fromFile(image));
            mContext.sendBroadcast(mediaScanIntent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void saveSaturateImage(Bitmap finalBitmap) {
        File file = new File(mContext.getFilesDir(), C.CACHEIMG);

        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 0, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFilePathFromUrl(String str) {
        String fileNameExt = str.substring(str.lastIndexOf('/') + 1,
                str.length());
        File wallCache = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath()
                + "/"
                + mContext.getApplicationContext().getString(R.string.app_name)
                + "/");
        return wallCache + "/" + fileNameExt;
    }

    public float getColorFloat(int num) {
        final String[] sa = mContext.getResources().getStringArray(R.array.satValues);
        return Float.parseFloat(sa[num]);
    }

    public float getFloat(int num) {
        final String[] sa = mContext.getResources().getStringArray(R.array.satValues);
        return Float.parseFloat(sa[num]);
    }

    public float h2F() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        final String[] sa = mContext.getResources().getStringArray(R.array.satValues);
        return Float.parseFloat(sa[hour]);
    }

    public String getHour(Integer in) {
        int hour = in;
        String shour = String.valueOf(in);
        if (!DateFormat.is24HourFormat(mContext)) {
            if (hour >= 12) {
                if (hour == 12) {
                    shour = 12 + " PM";
                } else {
                    shour = hour - 12 + " PM";
                }
            } else if (hour == 0) {
                shour = 12 + " AM";
            } else {
                shour = hour + " AM";
            }
        }

        return mContext.getResources().getString(R.string.wallpaper_at) + shour;
    }

    public File getExternalDir() {
        return new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + mContext.getString(R.string.app_name));
    }

    public void removeDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                new File(dir, children[i]).delete();
            }
        }
    }

    public void setWallpaperAlarms(Boolean run) {
        int INTERVAL = 43200000;

        /**
         * TODO: These will be set in arrays.xml and passed here
         * from the settings activity, or on boot.
         *
         * This sets the alarms for when to check for wallpaper updates.
         *
         * Time intervals:
         * 1 = Every 24 hours = 86400000
         * 2 = Every 12 hours = 43200000
         * 3 = Every 6 hours = 21600000
         * 4 = Every 3 hours = 10800000
         * 5 = Every 1 hour = 3600000
         * 6 = Every 30 mins = 1800000
         */

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Intent in1 = new Intent("com.lithidsw.wallbox.WALLPAPER_CHECK_UPDATES");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 9856, in1, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), INTERVAL, pi);
        if (!run) {
            am.cancel(pi);
        }
    }

    public void stopSaturatedAlarms() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Intent in = new Intent("com.lithidsw.wallbox.UPDATE_SATURATE_WALL");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, C.ALARM_ID, in, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(
                Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
        am.cancel(pi);
    }

    public void setSaturatedAlarms() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY) + 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Intent in = new Intent("com.lithidsw.wallbox.UPDATE_SATURATE_WALL");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, C.ALARM_ID, in, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(
                Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_HOUR, pi);
    }

    public void stopRandomizerAlarms() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Intent in = new Intent("com.lithidsw.wallbox.UPDATE_RANDOMIZER_WALL");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 9563, in, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(
                Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
        am.cancel(pi);
    }

    public void setRandomizerAlarms(int option) {
        long interval;
        switch (option) {
            case 0:
                interval = 0;
                break;
            case 1:
                interval = (AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15);
                break;
            case 2:
                interval = (AlarmManager.INTERVAL_HALF_HOUR / 15);
                break;
            case 3:
                interval = (AlarmManager.INTERVAL_FIFTEEN_MINUTES / 3);
                break;
            case 4:
                interval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
                break;
            case 5:
                interval = AlarmManager.INTERVAL_HALF_HOUR;
                break;
            case 6:
                interval = (AlarmManager.INTERVAL_HALF_HOUR + AlarmManager.INTERVAL_FIFTEEN_MINUTES);
                break;
            case 7:
                interval = AlarmManager.INTERVAL_HOUR;
                break;
            case 8:
                interval = (AlarmManager.INTERVAL_HOUR * 2);
                break;
            case 9:
                interval = (AlarmManager.INTERVAL_HOUR * 4);
                break;
            case 10:
                interval = (AlarmManager.INTERVAL_HALF_DAY / 2);
                break;
            case 11:
                interval = AlarmManager.INTERVAL_HALF_DAY;
                break;
            case 12:
                interval = AlarmManager.INTERVAL_DAY;
                break;
            default:
                interval = 0;
        }

        if (interval == 0) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.get(Calendar.SECOND));

        cal.add(Calendar.SECOND, 5);
        Intent in = new Intent("com.lithidsw.wallbox.UPDATE_RANDOMIZER_WALL");
        PendingIntent pi = PendingIntent.getBroadcast(mContext, 9563, in, 0);
        AlarmManager am = (AlarmManager) mContext.getSystemService(
                Activity.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
                interval, pi);
    }
}
