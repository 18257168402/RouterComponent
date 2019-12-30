package com.zb.component;

public class KRemoteComponnetInfo {
    public String action;
    public IRemoteComponent remote;
    public IRemoteComponent replyTo;
    public KRemoteComponnetInfo(String action,IRemoteComponent remote,IRemoteComponent replyTo){
        this.action = action;
        this.remote = remote;
        this.replyTo = replyTo;
    }
}
