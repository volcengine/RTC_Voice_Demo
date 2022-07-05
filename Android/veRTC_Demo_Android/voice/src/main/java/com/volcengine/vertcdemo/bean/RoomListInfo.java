package com.volcengine.vertcdemo.bean;

import java.util.List;

public class RoomListInfo extends VoiceResponse {

    public List<ChatRoomInfo> infos;

    public RoomListInfo(List<ChatRoomInfo> infos) {
        this.infos = infos;
    }
}
