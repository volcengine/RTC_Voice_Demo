package com.volcengine.vertcdemo.voice.event;

/**
 * 收到邀请上麦事件
 */
public class InviteMicEvent {
    public String userId;

    public InviteMicEvent(String userId) {
        this.userId = userId;
    }
}
