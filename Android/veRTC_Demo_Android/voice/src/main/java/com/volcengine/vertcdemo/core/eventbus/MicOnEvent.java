package com.volcengine.vertcdemo.core.eventbus;

import com.volcengine.vertcdemo.bean.ChatUserInfo;

public class MicOnEvent {
    public ChatUserInfo user;

    public MicOnEvent(ChatUserInfo user) {
        this.user = user;
    }
}
