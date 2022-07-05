package com.volcengine.vertcdemo.core.eventbus;

public class ChatEndEvent {
    public String roomId;

    public ChatEndEvent(String roomId) {
        this.roomId = roomId;
    }
}
