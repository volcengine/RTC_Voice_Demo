package com.volcengine.vertcdemo.voice.core;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.video.rtc.demo.basic_module.utils.AppExecutors;
import com.volcengine.vertcdemo.common.AbsBroadcast;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rts.RTSBaseClient;
import com.volcengine.vertcdemo.core.net.rts.RTSBizInform;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.voice.bean.ChatRoomInfo;
import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;
import com.volcengine.vertcdemo.voice.bean.CreateJoinRoomResult;
import com.volcengine.vertcdemo.voice.bean.GetUserList;
import com.volcengine.vertcdemo.voice.bean.HostChangeInfo;
import com.volcengine.vertcdemo.voice.bean.RoomListInfo;
import com.volcengine.vertcdemo.voice.bean.UserStatus;
import com.volcengine.vertcdemo.voice.bean.VoiceResponse;
import com.volcengine.vertcdemo.voice.event.ChatEndEvent;
import com.volcengine.vertcdemo.voice.event.InviteMicEvent;
import com.volcengine.vertcdemo.voice.event.JoinChatEvent;
import com.volcengine.vertcdemo.voice.event.LeaveChatEvent;
import com.volcengine.vertcdemo.voice.event.MicOffEvent;
import com.volcengine.vertcdemo.voice.event.MicOnEvent;
import com.volcengine.vertcdemo.voice.event.MuteMicEvent;
import com.volcengine.vertcdemo.voice.event.UserRaiseHandsEvent;
import com.volcengine.vertcdemo.voice.event.UnmuteMicEvent;

import java.util.UUID;

public class VoiceRTSClient extends RTSBaseClient {
    private static final String CREATE_CHAT_ROOM = "csCreateMeeting";
    private static final String JOIN_CHAT_ROOM = "csJoinMeeting";
    private static final String LEAVE_CHAT_ROOM = "csLeaveMeeting";
    private static final String GET_CHAT_ROOM_LIST = "csGetMeetings";
    private static final String GET_RAISE_HANDS = "csGetRaiseHands";
    private static final String GET_AUDIENCES = "csGetAudiences";
    private static final String RAISE_HANDS_MIC = "csRaiseHandsMic";
    private static final String OFF_SELF_MIC = "csOffSelfMic";
    private static final String CONFIRM_MIC = "csConfirmMic";
    private static final String OFF_MIC = "csOffMic";
    private static final String AGREE_MIC = "csAgreeMic";
    private static final String INVITE_MIC = "csInviteMic";
    private static final String MUTE_MIC = "csMuteMic";
    private static final String UNMUTE_MIC = "csUnmuteMic";

    public VoiceRTSClient(@NonNull RTCVideo rtcVideo, @NonNull RTSInfo rtmInfo) {
        super(rtcVideo, rtmInfo);
        initEventListener();
    }

    private <T extends VoiceResponse> void sendServerMessageOnNetwork(String roomId, JsonObject content, Class<T> resultClass, IRequestCallback<T> callback) {
        String cmd = content.get("event_name").getAsString();
        if (TextUtils.isEmpty(cmd)) {
            return;
        }
        AppExecutors.networkIO().execute(() -> sendServerMessage(cmd, roomId, content, resultClass, callback));
    }


    public void requestCreateRoom(String roomName, String userName, IRequestCallback<CreateJoinRoomResult> callback) {
        JsonObject params = getCommonParams(CREATE_CHAT_ROOM);
        params.addProperty("room_name", roomName);
        params.addProperty("user_name", userName);
        sendServerMessageOnNetwork(getRoomId(), params, CreateJoinRoomResult.class, callback);
    }


    public void requestJoinRoom(String roomId, String userName, IRequestCallback<CreateJoinRoomResult> callback) {
        JsonObject params = getCommonParams(JOIN_CHAT_ROOM);
        params.addProperty("room_id", roomId);
        params.addProperty("user_name", userName);
        sendServerMessageOnNetwork(getRoomId(), params, CreateJoinRoomResult.class, callback);
    }

    public void requestLeaveRoom() {
        JsonObject params = getCommonParams(LEAVE_CHAT_ROOM);
        sendServerMessageOnNetwork(getRoomId(), params, VoiceResponse.class, null);
    }

    public void requestRoomList(IRequestCallback<RoomListInfo> callback) {
        JsonObject params = getCommonParams(GET_CHAT_ROOM_LIST);
        sendServerMessageOnNetwork(getRoomId(), params, RoomListInfo.class, callback);
    }

    public void requestRaiseHandUserList(IRequestCallback<GetUserList> callback) {
        JsonObject params = getCommonParams(GET_RAISE_HANDS);
        sendServerMessageOnNetwork(getRoomId(), params, GetUserList.class, callback);
    }

    public void requestListenerUserList(IRequestCallback<GetUserList> callback) {
        JsonObject params = getCommonParams(GET_AUDIENCES);
        sendServerMessageOnNetwork(getRoomId(), params, GetUserList.class, callback);
    }

    public void requestRaiseHand() {
        JsonObject params = getCommonParams(RAISE_HANDS_MIC);
        sendServerMessageOnNetwork(getRoomId(), params, VoiceResponse.class, null);
    }

    public void requestBecomeListener() {
        JsonObject params = getCommonParams(OFF_SELF_MIC);
        sendServerMessageOnNetwork(getRoomId(), params, VoiceResponse.class, null);
    }

    public void confirmBecomeSpeaker() {
        JsonObject params = getCommonParams(CONFIRM_MIC);
        sendServerMessageOnNetwork(getRoomId(), params, VoiceResponse.class, null);
    }

    public void onUserOption(ChatUserInfo info) {
        if (info == null) {
            return;
        }
        int status = info.userStatus;
        String cmd = null;
        if (status == UserStatus.UserStatusOnMicrophone.getStatus()) {
            cmd = OFF_MIC;
        } else if (status == UserStatus.UserStatusRaiseHands.getStatus()) {
            cmd = AGREE_MIC;
        } else if (status == UserStatus.UserStatusAudience.getStatus()) {
            cmd = INVITE_MIC;
        }

        JsonObject params = getCommonParams(cmd);
        params.addProperty("user_id", info.userId);
        sendServerMessageOnNetwork(getRoomId(), params, VoiceResponse.class, null);
    }

    public void switchMic(boolean isOn) {
        String cmd = isOn ? UNMUTE_MIC : MUTE_MIC;
        JsonObject params = getCommonParams(cmd);
        sendServerMessageOnNetwork(getRoomId(), params, VoiceResponse.class, null);
        VoiceRTCManager.muteLocalAudioStream(!isOn);
    }


    private JsonObject getCommonParams(String cmd) {
        JsonObject params = new JsonObject();
        params.addProperty("app_id", mRtmInfo.appId);
        params.addProperty("room_id", VoiceDataManger.getRoomId());
        params.addProperty("user_id", SolutionDataManager.ins().getUserId());
        params.addProperty("event_name", cmd);
        params.addProperty("request_id", UUID.randomUUID().toString());
        params.addProperty("device_id", SolutionDataManager.ins().getDeviceId());
        return params;
    }

    private String getRoomId() {
        return VoiceDataManger.getRoomId();
    }

    private static final String ON_JOIN_CHAT = "onCsJoinMeeting";
    private static final String ON_LEAVE_CHAT = "onCsLeaveMeeting";
    private static final String ON_RAISE_HANDS = "onCsRaiseHandsMic";
    private static final String ON_INVITE_MIC = "onCsInviteMic";
    private static final String ON_MIC_ON = "onCsMicOn";
    private static final String ON_MIC_OFF = "onCsMicOff";
    private static final String ON_MIC_MUTE = "onCsMuteMic";
    private static final String ON_MIC_UN_MUTE = "onCsUnmuteMic";
    private static final String ON_CHAT_END = "onCsMeetingEnd";
    private static final String ON_HOST_CHANGE = "onCsHostChange";

    private void initEventListener() {
        putEventListener(new AbsBroadcast<>(ON_JOIN_CHAT, ChatUserInfo.class,
                data -> {
                    if (!TextUtils.isEmpty(data.userId) && !VoiceDataManger.isSelf(data.userId)) {
                        SolutionDemoEventManager.post(new JoinChatEvent(0, data));
                    }
                }));

        putEventListener(new AbsBroadcast<>(ON_LEAVE_CHAT, ChatUserInfo.class,
                data -> {
                    if (!TextUtils.isEmpty(data.userId) && !VoiceDataManger.isSelf(data.userId)) {
                        SolutionDemoEventManager.post(new LeaveChatEvent(data));
                    }
                }));

        putEventListener(new AbsBroadcast<>(ON_RAISE_HANDS, ChatUserInfo.class,
                data -> {
                    if (!TextUtils.isEmpty(data.userId)) {
                        SolutionDemoEventManager.post(new UserRaiseHandsEvent(data.userId));
                    }
                }));

        putEventListener(new AbsBroadcast<>(ON_INVITE_MIC, ChatUserInfo.class,
                data -> {
                    if (!TextUtils.isEmpty(data.userId) && VoiceDataManger.isSelf(data.userId)) {
                        SolutionDemoEventManager.post(new InviteMicEvent(data.userId));
                    }
                }));

        putEventListener(new AbsBroadcast<>(ON_MIC_ON, ChatUserInfo.class,
                data -> SolutionDemoEventManager.post(new MicOnEvent(data))));

        putEventListener(new AbsBroadcast<>(ON_MIC_OFF, ChatUserInfo.class,
                data -> SolutionDemoEventManager.post(new MicOffEvent(data.userId))));

        putEventListener(new AbsBroadcast<>(ON_MIC_MUTE, ChatUserInfo.class,
                data -> SolutionDemoEventManager.post(new MuteMicEvent(data.userId))));

        putEventListener(new AbsBroadcast<>(ON_MIC_UN_MUTE, ChatUserInfo.class,
                data -> SolutionDemoEventManager.post(new UnmuteMicEvent(data.userId))));

        putEventListener(new AbsBroadcast<>(ON_CHAT_END, ChatRoomInfo.class,
                data -> SolutionDemoEventManager.post(new ChatEndEvent(data.roomId))));

        putEventListener(new AbsBroadcast<>(ON_HOST_CHANGE, HostChangeInfo.class,
                SolutionDemoEventManager::post));
    }

    private void putEventListener(AbsBroadcast<? extends RTSBizInform> absBroadcast) {
        mEventListeners.put(absBroadcast.getEvent(), absBroadcast);
    }

    public void removeAllEventListener() {
        mEventListeners.remove(ON_JOIN_CHAT);
        mEventListeners.remove(ON_LEAVE_CHAT);
        mEventListeners.remove(ON_RAISE_HANDS);
        mEventListeners.remove(ON_INVITE_MIC);
        mEventListeners.remove(ON_MIC_ON);
        mEventListeners.remove(ON_MIC_OFF);
        mEventListeners.remove(ON_MIC_MUTE);
        mEventListeners.remove(ON_MIC_UN_MUTE);
        mEventListeners.remove(ON_CHAT_END);
        mEventListeners.remove(ON_HOST_CHANGE);
    }
}
