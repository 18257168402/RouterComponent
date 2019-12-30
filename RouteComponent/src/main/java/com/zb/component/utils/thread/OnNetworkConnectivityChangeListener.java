package com.zb.component.utils.thread;

import android.net.NetworkInfo;

public interface OnNetworkConnectivityChangeListener {
    void onNetworkConnectivityChange(NetworkInfo info);
}
