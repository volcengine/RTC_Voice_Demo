package com.volcengine.vertcdemo.bean;

import java.util.List;

public class GetUserList extends VoiceResponse {

    public List<ChatUserInfo> users;

    public GetUserList(List<ChatUserInfo> users) {
        this.users = users;
    }
}
