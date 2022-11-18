package com.volcengine.vertcdemo.voice.core;

import android.text.TextUtils;

import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.voice.bean.CreateJoinRoomResult;

public class VoiceDataManger {
    public static CreateJoinRoomResult sCreateRoomResult = null;

    public static void release() {
        sCreateRoomResult = null;
    }

    public static boolean isSelf(String uid) {
        return !TextUtils.isEmpty(uid) && TextUtils.equals(uid, getUid());
    }

    public static void setRoomInfo(CreateJoinRoomResult result) {
        sCreateRoomResult = result;
    }

    public static String getRoomId() {
        if (sCreateRoomResult == null || sCreateRoomResult.info == null) {
            return null;
        }
        return sCreateRoomResult.info.roomId;
    }

    public static String getUid() {
        return SolutionDataManager.ins().getUserId();
    }
}
