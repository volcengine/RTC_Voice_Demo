// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;

/**
 * 麦克风打开事件
 */
public class MicOnEvent {
    public ChatUserInfo user;

    public MicOnEvent(ChatUserInfo user) {
        this.user = user;
    }
}
