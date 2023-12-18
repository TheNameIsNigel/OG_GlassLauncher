package com.google.android.glass.launcher.indicators;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.google.android.glass.launcher.viewmodels.IndicatorsLifecycleObserver;
import com.google.android.glass.launcher.viewmodels.IndicatorsViewModel;
import com.google.android.glass.ui.GlassLifecycleFragment;
import java.util.HashMap;
import java.util.Map;

public class IndicatorsFragment extends GlassLifecycleFragment {
    private IndicatorsLifecycleObserver indicatorsLifecycleObserver;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(2131361836, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        IndicatorsViewModel indicatorsViewModel = (IndicatorsViewModel) ViewModelProviders.of(getActivity()).get(IndicatorsViewModel.class);
        this.indicatorsLifecycleObserver = new IndicatorsLifecycleObserver(getContext(), indicatorsViewModel);
        getLifecycle().addObserver(this.indicatorsLifecycleObserver);
        Map<ImageView, LiveData<Integer>> indicatorsMap = new HashMap<>();
        indicatorsMap.put((ImageView) view.findViewById(2131230821), indicatorsViewModel.getWifiLevel());
        indicatorsMap.put((ImageView) view.findViewById(2131230817), indicatorsViewModel.getBatteryLevel());
        indicatorsMap.put((ImageView) view.findViewById(2131230818), indicatorsViewModel.getBluetoothLevel());
        indicatorsMap.put((ImageView) view.findViewById(2131230820), indicatorsViewModel.getVolumeLevel());
        for (Map.Entry<ImageView, LiveData<Integer>> entry : indicatorsMap.entrySet()) {
            ImageView key = entry.getKey();
            key.getClass();
            entry.getValue().observe(this, new $Lambda$sbmy5At8QXwuWXn1NU2w_ZV6ps(key));
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        getLifecycle().removeObserver(this.indicatorsLifecycleObserver);
    }
}
