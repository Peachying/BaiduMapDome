package com.skyfin.baidumapdome.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.skyfin.baidumapdome.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LocationSelectFragment extends Fragment {


    public LocationSelectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_location_select, container, false);
    }


}
