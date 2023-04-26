// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

/**
 * 房主收到用户请求上麦事件
 */
public class UserRaiseHandsEvent {
    public String userId;

    public UserRaiseHandsEvent(String userId) {
        this.userId = userId;
    }
}
