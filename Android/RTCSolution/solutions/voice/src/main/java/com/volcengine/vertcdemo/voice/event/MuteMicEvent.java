// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

/**
 * 麦克风被房主关闭事件
 */
public class MuteMicEvent {
    public String userId;

    public MuteMicEvent(String userId) {
        this.userId = userId;
    }
}
