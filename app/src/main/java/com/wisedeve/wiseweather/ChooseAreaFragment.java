package com.wisedeve.wiseweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Damon on 2017/4/7.
 * Description :
 */

public class ChooseAreaFragment extends Fragment {
    private TextView title;
    private ImageView back;
    private ListView listView;
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY= 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressBar progressBar;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);

        return view;
    }
}
