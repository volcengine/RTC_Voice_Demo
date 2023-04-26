// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.bean;

public enum UserStatus {
    UserStatusAudience(0),
    UserStatusRaiseHands(1),
    UserStatusOnMicrophone(2);

    private final int value;

    UserStatus(int value) {
        this.value = value;
    }

    public int getStatus() {
        return value;
    }
}
