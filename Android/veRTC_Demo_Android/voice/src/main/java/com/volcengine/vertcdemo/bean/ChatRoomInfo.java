package com.volcengine.vertcdemo.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class ChatRoomInfo implements RTMBizInform {

    @SerializedName("room_id")
    public String roomId;
    @SerializedName("room_name")
    public String roomName;
    @SerializedName("host_id")
    public String hostUserId;
    @SerializedName("host_name")
    public String hostUserName;
    @SerializedName("user_counts")
    public int userCount;
    @SerializedName("micon_counts")
    public int micOnCount;
    @SerializedName("now")
    public long now;
    @SerializedName("created_at")
    public long createdAt;

    public String getDecodedRoomName() {
        try {
            return URLDecoder.decode(roomName, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return roomName;
        }
    }

    @Override
    public String toString() {
        return "ChatRoomInfo{" +
                "roomId='" + roomId + '\'' +
                ", roomName='" + roomName + '\'' +
                ", hostUserId='" + hostUserId + '\'' +
                ", hostUserName='" + hostUserName + '\'' +
                ", userCount=" + userCount +
                ", micOnCount=" + micOnCount +
                ", now=" + now +
                ", createdAt=" + createdAt +
                '}';
    }
}
