package com.volcengine.vertcdemo.bean;

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
