package com.volcengine.vertcdemo.bean;

import java.util.List;

public class CreateJoinRoomResult extends VoiceResponse {
    public String token;
    public ChatRoomInfo info;
    public List<ChatUserInfo> users;

    @Override
    public String toString() {
        return "CreateJoinRoomResult{" +
                "token='" + token + '\'' +
                ", info=" + info +
                ", users=" + users +
                '}';
    }
}
