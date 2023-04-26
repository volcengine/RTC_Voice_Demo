// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.bean;

import java.util.List;

public class GetUserList extends VoiceResponse {

    public List<ChatUserInfo> users;

    public GetUserList(List<ChatUserInfo> users) {
        this.users = users;
    }
}
