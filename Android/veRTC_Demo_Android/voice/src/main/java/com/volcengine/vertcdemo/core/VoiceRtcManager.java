package com.volcengine.vertcdemo.core;

import static com.ss.bytertc.engine.RTCEngine.SubscribeFallbackOptions.SUBSCRIBE_FALLBACK_OPTIONS_AUDIO_ONLY;

import android.util.Log;

import com.ss.bytertc.engine.PublisherConfiguration;
import com.ss.bytertc.engine.RTCEngine;
import com.ss.bytertc.engine.SubscribeConfig;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.data.AudioPlaybackDevice;
import com.ss.bytertc.engine.data.MuteState;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.common.MLog;
import com.volcengine.vertcdemo.core.eventbus.SDKJoinChannelSuccessEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.eventbus.VoiceVolumeEvent;
import com.volcengine.vertcdemo.core.net.rtm.RTCEventHandlerWithRTM;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;

public class VoiceRtcManager {
    private static final String TAG = "VoiceRtcManager";
    public static final int VOLUME_INTERVAL_MS = 1000;
    public static final int VOLUME_SMOOTH = 8;
    private static RTCEngine sInstance = null;
    private static VoiceRtmClient rtmClient;

    private static boolean sIsFront;

    private static final RTCEventHandlerWithRTM sRtcEventHandler = new RTCEventHandlerWithRTM() {

        private RemoteAudioStats mAudioStats = new RemoteAudioStats();
        private RemoteVideoStats mVideoStats = new RemoteVideoStats();

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            Log.d(TAG, String.format("onRoomStateChanged: %s, %s, %d, %s", roomId, uid, state, extraInfo));
            if (joinRoomSuccessWhenFirst(state, extraInfo)) {
                SolutionDemoEventManager.post(new SDKJoinChannelSuccessEvent(roomId, uid));
            }
        }

        @Override
        public void onRemoteStreamStats(RemoteStreamStats stats) {
            super.onRemoteStreamStats(stats);
            mAudioStats = stats.audioStats;
            mVideoStats = stats.videoStats;
        }

        @Override
        public void onLocalStreamStats(LocalStreamStats stats) {
            super.onLocalStreamStats(stats);
            SolutionDemoEventManager.post(stats.audioStats);
        }

        @Override
        public void onAudioVolumeIndication(AudioVolumeInfo[] speakers, int totalVolume) {
            if (speakers == null || speakers.length == 0) {
                return;
            }
            SolutionDemoEventManager.post(new VoiceVolumeEvent(speakers, totalVolume));
        }

        @Override
        public void onError(int err) {
            Log.e("RTCManager", "onError:" + err);
        }
    };

    public static void createEngine(RtmInfo info) {
        if (sInstance == null) {
            try {
                sInstance = RTCEngine.createEngine(Utilities.getApplicationContext(), info.appId, sRtcEventHandler, null, null);
                rtmClient = new VoiceRtmClient(sInstance, info);
                sRtcEventHandler.setBaseClient(rtmClient);
                enableLocalAudio(true);
                setRemoteSubscribeFallbackOption(SUBSCRIBE_FALLBACK_OPTIONS_AUDIO_ONLY);
                enableAudioVolumeIndication(VOLUME_INTERVAL_MS, VOLUME_SMOOTH);
            } catch (Exception e) {
                MLog.e("createEngine", "", e);
            }
        }
    }

    public static VoiceRtmClient getRtmClient() {
        return rtmClient;
    }

    public static void destroyEngine() {
        MLog.d("destroyEngine", "");
        RTCEngine.destroyEngine(sInstance);
        RTCEngine.destroy();
        if (sInstance != null) {
            sInstance = null;
        }
        if (rtmClient != null) {
            rtmClient = null;
        }
    }

    public static void muteLocalAudioStream(boolean mute) {
        MLog.d("muteLocalAudioStream", "");
        if (sInstance == null) {
            return;
        }
        sInstance.muteLocalAudio(mute ? MuteState.MUTE_STATE_ON : MuteState.MUTE_STATE_OFF);
    }

    public static void enableLocalAudio(boolean enable) {
        MLog.d("enableLocalAudio", "");
        if (sInstance == null) {
            return;
        }
        if (enable) {
            sInstance.startAudioCapture();
        } else {
            sInstance.stopAudioCapture();
        }
    }

    public static void joinChannel(String token, String roomId, PublisherConfiguration configuration, String uid) {
        MLog.d("joinChannel", "token:" + token + " roomId:" + roomId + " uid:" + uid);
        if (sInstance == null) {
            return;
        }
        UserInfo userInfo = new UserInfo(uid, null);
        sInstance.joinRoom(token, roomId, userInfo, RTCEngine.ChannelProfile.CHANNEL_PROFILE_COMMUNICATION);
    }

    public static void leaveChannel() {
        MLog.d("leaveChannel", "");
        if (sInstance == null) {
            return;
        }
        sInstance.leaveRoom();
    }

    public static void setEnableSpeakerphone(boolean open) {
        MLog.d("setEnableSpeakerphone", "open");
        if (sInstance == null) {
            return;
        }
        sInstance.setAudioPlaybackDevice(open
                ? AudioPlaybackDevice.AUDIO_PLAYBACK_DEVICE_SPEAKERPHONE
                : AudioPlaybackDevice.AUDIO_PLAYBACK_DEVICE_EARPIECE);
    }

    public static void enableAudioVolumeIndication(int interval, int smooth) {
        MLog.d("enableAudioVolumeIndication", "" + interval + ":" + smooth);
        if (sInstance == null) {
            return;
        }
        sInstance.setAudioVolumeIndicationInterval(interval);
    }

    public static void subscribeStream(String uid, SubscribeConfig config) {
        MLog.d("subscribeStream", "" + uid + ":" + config.toString());
        if (sInstance == null) {
            return;
        }
        sInstance.subscribeStream(uid, config);
    }

    public static void unSubscribe(String uid, boolean isScreen) {
        MLog.d("unSubscribe", "" + uid + ":" + isScreen);
        if (sInstance == null) {
            return;
        }
        sInstance.unSubscribe(uid, isScreen);
    }

    public static void setRemoteSubscribeFallbackOption(RTCEngine.SubscribeFallbackOptions options) {
        MLog.d("setRemoteSubscribeFallbackOption", "" + options.toString());
        if (sInstance == null) {
            return;
        }
        sInstance.setSubscribeFallbackOption(options);
    }

}
