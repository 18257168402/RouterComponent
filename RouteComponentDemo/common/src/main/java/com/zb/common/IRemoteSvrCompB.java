package com.zb.common;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.zb.component.IRemoteListener;

public interface IRemoteSvrCompB extends IProvider {
    void testRemote();
    void testRemoteCallback(IRemoteCallback listener);
    void testRemoteEventBus();
}
