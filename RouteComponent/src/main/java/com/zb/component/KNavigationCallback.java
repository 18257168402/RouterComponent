package com.zb.component;

import com.alibaba.android.arouter.facade.Postcard;
import com.alibaba.android.arouter.facade.callback.NavigationCallback;

public interface KNavigationCallback extends NavigationCallback {
    void onResult(Postcard postcard, KNavResult bundle);
    void onTimeout(Postcard postcard);
}
