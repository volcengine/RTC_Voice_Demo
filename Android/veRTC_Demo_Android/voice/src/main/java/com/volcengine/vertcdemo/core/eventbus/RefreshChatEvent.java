package com.volcengine.vertcdemo.core.eventbus;

import com.volcengine.vertcdemo.bean.ChatRoomInfo;

import java.util.List;

public class RefreshChatEvent {

    public List<ChatRoomInfo> meetings;

    public RefreshChatEvent(List<ChatRoomInfo> meetings) {
        this.meetings = meetings;
    }
}
