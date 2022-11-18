package com.volcengine.vertcdemo.voice.bean;

import java.util.List;

public class RoomListInfo extends VoiceResponse {

    public List<ChatRoomInfo> infos;

    public RoomListInfo(List<ChatRoomInfo> infos) {
        this.infos = infos;
    }
}
