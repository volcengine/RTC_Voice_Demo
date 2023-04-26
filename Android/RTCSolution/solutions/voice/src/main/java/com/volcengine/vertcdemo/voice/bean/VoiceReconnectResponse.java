// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.bean;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class VoiceReconnectResponse extends VoiceResponse{

    @SerializedName("token")
    public String token;
    @SerializedName("info")
    public ChatRoomInfo info;
    @SerializedName("users")
    public List<ChatUserInfo> users;

    public VoiceReconnectResponse() {

    }
}
