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
