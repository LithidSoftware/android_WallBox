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

package com.lithidsw.wallbox.app.theme;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lithidsw.wallbox.R;

public class ThemeFragment extends Fragment {

    ImageView mThemePreview;
    FragmentActivity fa;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fa = super.getActivity();
        LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.theme_frag, container, false);
        if (ll != null) {
            mThemePreview = (ImageView) ll.findViewById(R.id.theme_preview);
            int[] themes = getArguments().getIntArray("theme_preview");
            mThemePreview.setImageResource(isPort()? themes[0] : themes[1]);
        }
        return ll;
    }


    private boolean isPort() {
        return fa.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }
}
