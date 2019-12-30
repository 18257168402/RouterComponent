package com.zb.component.plugins;

import com.zb.component.IEventBus;
import com.zb.component.IEventBusFactory;

import org.greenrobot.eventbus.EventBus;


public class EventBusFactory implements IEventBusFactory {
    @Override
    public IEventBus eventBus() {
        return new IEventBus() {
            @Override
            public void register(Object object) {
                EventBus.getDefault().register(object);
            }

            @Override
            public void unregister(Object object) {
                EventBus.getDefault().unregister(object);
            }

            @Override
            public void post(Object event) {
                EventBus.getDefault().post(event);
            }
        };
    }
}
