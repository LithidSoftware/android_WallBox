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

package com.lithidsw.wallbox.app.randomizer.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.lithidsw.wallbox.R;
import com.lithidsw.wallbox.loader.ImageLoader;

public class RandomizerGridAdapter extends BaseAdapter {

    private LayoutInflater inflater = null;
    Activity activity;
    private List<String[]> items = new ArrayList<String[]>();
    View vi;

    ImageLoader imageLoader;

    public RandomizerGridAdapter(Activity a, List<String[]> b) {
        activity = a;
        items = b;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        imageLoader = new ImageLoader(activity, 200);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        vi = view;
        if (view == null) {
            vi = inflater.inflate(R.layout.randomizer_items, null);
        }

        final String image_path = items.get(i)[0];

        ImageView imageView = (ImageView) vi.findViewById(R.id.picture);
        imageLoader.DisplayImage(image_path, imageView);

        return vi;
    }
}
