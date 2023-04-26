// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.core;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.voice.bean.ChatRoomInfo;
import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;
import com.volcengine.vertcdemo.voice.bean.CreateJoinRoomResult;
import com.volcengine.vertcdemo.voice.bean.VoiceReconnectResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VoiceDataManger {

    public static String sToken;
    public static ChatRoomInfo sRoomInfo;

    @NonNull
    private static List<ChatUserInfo> sUserList = Collections.emptyList();

    public static void release() {
        sRoomInfo = null;
        sUserList = Collections.emptyList();
    }

    public static boolean isSelf(String uid) {
        return !TextUtils.isEmpty(uid) && TextUtils.equals(uid, getUid());
    }

    @NonNull
    public static List<ChatUserInfo> getUserList() {
        return new ArrayList<>(sUserList);
    }

    public static String getRoomId() {
        if (sRoomInfo == null) {
            return null;
        }
        return sRoomInfo.roomId;
    }

    public static String getUid() {
        return SolutionDataManager.ins().getUserId();
    }

    public static void setInfo(@NonNull CreateJoinRoomResult data) {
        sToken = data.token;
        sRoomInfo = data.info;
        setUserList(data.users);
    }

    public static void setInfo(VoiceReconnectResponse data) {
        sToken = data.token;
        sRoomInfo = data.info;
        setUserList(data.users);
    }

    public static void clearInfo() {
        sRoomInfo = null;
        setUserList(null);
    }

    private static void setUserList(List<ChatUserInfo> users) {
        sUserList = users == null ? Collections.emptyList() : users;
    }
}
