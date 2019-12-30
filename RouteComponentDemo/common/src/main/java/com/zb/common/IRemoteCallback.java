package com.zb.common;

import com.zb.component.IRemoteListener;

public interface IRemoteCallback extends IRemoteListener {
    void onSuccess(String str);
    void onError(int code);
}
