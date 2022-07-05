package com.volcengine.vertcdemo.core.eventbus;

import com.volcengine.vertcdemo.bean.ChatUserInfo;

import java.util.List;

public class RefreshListenerEvent {
    public List<ChatUserInfo> users;

    public RefreshListenerEvent(List<ChatUserInfo> users) {
        this.users = users;
    }
}
