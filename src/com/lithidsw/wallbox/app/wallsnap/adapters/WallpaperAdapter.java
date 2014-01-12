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

package com.lithidsw.wallbox.app.wallsnap.adapters;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.loader.ImageLoader;
import com.lithidsw.wallbox.utils.C;

public class WallpaperAdapter extends BaseAdapter {

	private Activity activity;
	private List<String[]> mWallpapers = new ArrayList<String[]>();
	private static LayoutInflater inflater = null;
	public ImageLoader imageLoader;
	View vi;
	SharedPreferences prefs;

	public WallpaperAdapter(Activity a, List<String[]> b) {
		activity = a;
		mWallpapers = b;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		imageLoader = new ImageLoader(activity.getApplicationContext(), 500);
		prefs = activity.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
	}

	@Override
	public int getCount() {
		try {
			return mWallpapers.size();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.wallpaper_item, null);
		}

		final String path = mWallpapers.get(position)[0];
		final String name = mWallpapers.get(position)[1];

		final ImageView imgpreview = (ImageView) vi.findViewById(R.id.preview);
		imageLoader.DisplayImage(path, imgpreview);

		final TextView txtname = (TextView) vi.findViewById(R.id.name);
		txtname.setText(name);


		return vi;
	}
}
