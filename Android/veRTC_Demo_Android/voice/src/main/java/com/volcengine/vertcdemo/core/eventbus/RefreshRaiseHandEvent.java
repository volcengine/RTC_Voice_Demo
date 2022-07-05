package com.volcengine.vertcdemo.core.eventbus;

import com.volcengine.vertcdemo.bean.ChatUserInfo;

import java.util.List;

public class RefreshRaiseHandEvent {
    public List<ChatUserInfo> users;

    public RefreshRaiseHandEvent(List<ChatUserInfo> users) {
        this.users = users;
    }
}
