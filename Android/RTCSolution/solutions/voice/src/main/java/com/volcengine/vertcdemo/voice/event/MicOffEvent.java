// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

/**
 * 麦克风关闭事件
 */
public class MicOffEvent {
    public String userId;

    public MicOffEvent(String userId) {
        this.userId = userId;
    }
}
