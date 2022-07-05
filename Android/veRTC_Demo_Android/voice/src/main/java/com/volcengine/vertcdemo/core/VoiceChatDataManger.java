package com.volcengine.vertcdemo.core;

import android.text.TextUtils;

import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.bean.CreateJoinRoomResult;

public class VoiceChatDataManger {
    public static CreateJoinRoomResult sCreateRoomResult = null;

    public static void init(RtmInfo rtmInfo) {
        if (rtmInfo == null || !rtmInfo.isValid()) {
            return;
        }
        VoiceRtcManager.createEngine(rtmInfo);
    }

    public static void release() {
        VoiceRtcManager.destroyEngine();
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
