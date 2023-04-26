// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

/**
 * 取消用户静音事件
 */
public class UnmuteMicEvent {
    public String userId;

    public UnmuteMicEvent(String userId) {
        this.userId = userId;
    }
}
