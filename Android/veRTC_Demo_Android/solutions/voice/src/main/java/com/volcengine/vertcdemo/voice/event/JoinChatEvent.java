package com.volcengine.vertcdemo.voice.event;

import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;

/**
 * 用户加入语聊房事件
 */
public class JoinChatEvent {

    public int errorCode;
    public ChatUserInfo user;

    public JoinChatEvent(int errorCode, ChatUserInfo user) {
        this.errorCode = errorCode;
        this.user = user;
    }
}
