package com.volcengine.vertcdemo.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class ChatUserInfo implements RTMBizInform {

    @SerializedName("user_id")
    public String userId;
    @SerializedName("user_name")
    public String userName;
    @SerializedName("user_status")
    public int userStatus;
    @SerializedName("is_mic_on")
    public boolean isMicOn;
    @SerializedName("is_host")
    public boolean isHost;

    @Override
    public String toString() {
        return "ChatUserInfo{" +
                "userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", userStatus=" + userStatus +
                ", isMicOn=" + isMicOn +
                ", isHost=" + isHost +
                '}';
    }
}
