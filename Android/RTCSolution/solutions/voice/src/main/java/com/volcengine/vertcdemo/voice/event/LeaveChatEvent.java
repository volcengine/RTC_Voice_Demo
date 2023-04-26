// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;

/**
 * 用户离开语聊房事件
 */
public class LeaveChatEvent {
    public ChatUserInfo user;

    public LeaveChatEvent(ChatUserInfo user) {
        this.user = user;
    }
}
