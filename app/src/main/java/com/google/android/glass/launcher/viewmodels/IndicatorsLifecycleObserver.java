package com.google.android.glass.launcher.viewmodels;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import com.google.android.glass.utils.IntentUtils;
import java.util.Locale;

public class IndicatorsLifecycleObserver implements LifecycleObserver {
    private static final int BLUETOOTH_DISABLED_LEVEL = 0;
    private static final int BLUETOOTH_ENABLED_LEVEL = 1;
    private static final double NUMBER_OF_VOLUME_LEVELS = 15.0d;
    private static final String PERCENT_STRING_FORMAT = "%d%s";
    private static final String QUOTE_REGEX = "^\"(.*)\"$";
    private static final int VOLUME_LEVEL_MUTE = 0;
    private static final int WIFI_DISABLED_LEVEL = 5;
    private static final int WIFI_NO_SIGNAL_LEVEL = 0;
    private static final int WIFI_NUMBER_OF_LEVELS = 5;
    private static final String WIFI_UNKNOWN_SSID = "<unknown ssid>";
    private final AudioManager audioManager;
    private final BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IndicatorsLifecycleObserver.this.updateBatteryStatus();
        }
    };
    private final BatteryManager batteryManager;
    private final ConnectivityManager connectivityManager;
    private final Context context;
    private final IndicatorsViewModel indicatorsViewModel;
    private final BroadcastReceiver wifiBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            IndicatorsLifecycleObserver.this.updateWifiStatus();
        }
    };
    private final WifiManager wifiManager;

    public IndicatorsLifecycleObserver(Context context2, IndicatorsViewModel indicatorsViewModel2) {
        this.context = context2;
        this.indicatorsViewModel = indicatorsViewModel2;
        this.audioManager = (AudioManager) context2.getSystemService("audio");
        this.batteryManager = (BatteryManager) context2.getSystemService("batterymanager");
        this.wifiManager = (WifiManager) context2.getApplicationContext().getSystemService("wifi");
        this.connectivityManager = (ConnectivityManager) context2.getSystemService("connectivity");
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        this.context.registerReceiver(this.wifiBroadcastReceiver, IntentUtils.getWifiIntentFilter());
        this.context.registerReceiver(this.batteryBroadcastReceiver, IntentUtils.getBatteryIntentFilter());
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        this.context.unregisterReceiver(this.wifiBroadcastReceiver);
        this.context.unregisterReceiver(this.batteryBroadcastReceiver);
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void updateBatteryStatus() {
        int batteryLevel = this.batteryManager.getIntProperty(4);
        this.indicatorsViewModel.setBatteryLevel(batteryLevel);
        this.indicatorsViewModel.setBatteryDescription(String.format(Locale.US, PERCENT_STRING_FORMAT, new Object[]{Integer.valueOf(batteryLevel), this.context.getString(2131492914)}));
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void updateBluetoothStatus() {
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            this.indicatorsViewModel.setBluetoothLevel(1);
            this.indicatorsViewModel.setBluetoothDescription(this.context.getString(2131492908));
            return;
        }
        this.indicatorsViewModel.setBluetoothLevel(0);
        this.indicatorsViewModel.setBluetoothDescription(this.context.getString(2131492907));
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void updateVolumeStatus() {
        int streamVolume = this.audioManager.getStreamVolume(3);
        this.indicatorsViewModel.setVolumeLevel(streamVolume);
        if (streamVolume == 0) {
            this.indicatorsViewModel.setVolumeDescription(this.context.getString(2131492906));
            return;
        }
        int volumePercent = (int) Math.round(((double) (streamVolume * 100)) / NUMBER_OF_VOLUME_LEVELS);
        this.indicatorsViewModel.setVolumeDescription(String.format(Locale.US, PERCENT_STRING_FORMAT, new Object[]{Integer.valueOf(volumePercent), this.context.getString(2131492914)}));
    }

    /* access modifiers changed from: protected */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void updateWifiStatus() {
        int i = 5;
        NetworkInfo networkInfo = this.connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getType() != 1 || !networkInfo.isConnected()) {
            boolean wifiEnabled = this.wifiManager.isWifiEnabled();
            IndicatorsViewModel indicatorsViewModel2 = this.indicatorsViewModel;
            if (wifiEnabled) {
                i = 0;
            }
            indicatorsViewModel2.setWifiLevel(i);
            this.indicatorsViewModel.setWifiDescription(wifiEnabled ? "" : this.context.getString(2131492907));
            return;
        }
        this.indicatorsViewModel.setWifiLevel(WifiManager.calculateSignalLevel(this.wifiManager.getConnectionInfo().getRssi(), 5));
        String wifiSsidWithoutQuotes = this.wifiManager.getConnectionInfo().getSSID().replaceFirst(QUOTE_REGEX, "$1");
        IndicatorsViewModel indicatorsViewModel3 = this.indicatorsViewModel;
        if (WIFI_UNKNOWN_SSID.equals(wifiSsidWithoutQuotes)) {
            wifiSsidWithoutQuotes = "";
        }
        indicatorsViewModel3.setWifiDescription(wifiSsidWithoutQuotes);
    }
}
