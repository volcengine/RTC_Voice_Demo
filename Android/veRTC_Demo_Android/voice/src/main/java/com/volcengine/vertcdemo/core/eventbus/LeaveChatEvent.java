package com.volcengine.vertcdemo.core.eventbus;

import com.volcengine.vertcdemo.bean.ChatUserInfo;

public class LeaveChatEvent {
    public ChatUserInfo user;

    public LeaveChatEvent(ChatUserInfo user) {
        this.user = user;
    }
}
