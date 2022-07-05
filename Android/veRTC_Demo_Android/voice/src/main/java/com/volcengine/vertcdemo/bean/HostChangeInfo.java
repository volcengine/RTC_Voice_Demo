package com.volcengine.vertcdemo.bean;

import com.google.gson.annotations.SerializedName;
import com.volcengine.vertcdemo.core.net.rtm.RTMBizInform;

public class HostChangeInfo implements RTMBizInform {
    @SerializedName("former_host_id")
    public String formerHostId;
    @SerializedName("host_info")
    public ChatUserInfo hostInfo;

    public HostChangeInfo(String formerHostId, ChatUserInfo hostInfo) {
        this.formerHostId = formerHostId;
        this.hostInfo = hostInfo;
    }
}
