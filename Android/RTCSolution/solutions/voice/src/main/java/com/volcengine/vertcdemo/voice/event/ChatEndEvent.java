// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

/**
 * 聊天房关闭事件
 */
public class ChatEndEvent {
    public String roomId;

    public ChatEndEvent(String roomId) {
        this.roomId = roomId;
    }
}
