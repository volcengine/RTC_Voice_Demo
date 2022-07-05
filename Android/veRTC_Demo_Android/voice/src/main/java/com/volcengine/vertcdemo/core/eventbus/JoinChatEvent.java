package com.volcengine.vertcdemo.core.eventbus;

import com.volcengine.vertcdemo.bean.ChatUserInfo;

public class JoinChatEvent {

    public int errorCode;
    public ChatUserInfo user;

    public JoinChatEvent(int errorCode, ChatUserInfo user) {
        this.errorCode = errorCode;
        this.user = user;
    }
}
