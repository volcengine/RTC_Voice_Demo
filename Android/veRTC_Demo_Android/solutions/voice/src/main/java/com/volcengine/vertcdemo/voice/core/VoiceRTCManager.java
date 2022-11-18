package com.volcengine.vertcdemo.voice.core;

import static com.ss.bytertc.engine.type.SubscribeFallbackOptions.SUBSCRIBE_FALLBACK_OPTIONS_AUDIO_ONLY;

import android.util.Log;

import com.ss.bytertc.engine.RTCRoom;
import com.ss.bytertc.engine.RTCRoomConfig;
import com.ss.bytertc.engine.RTCVideo;
import com.ss.bytertc.engine.UserInfo;
import com.ss.bytertc.engine.data.AudioPropertiesConfig;
import com.ss.bytertc.engine.data.LocalAudioPropertiesInfo;
import com.ss.bytertc.engine.data.RemoteAudioPropertiesInfo;
import com.ss.bytertc.engine.data.StreamIndex;
import com.ss.bytertc.engine.type.ChannelProfile;
import com.ss.bytertc.engine.type.LocalStreamStats;
import com.ss.bytertc.engine.type.MediaStreamType;
import com.ss.bytertc.engine.type.RemoteStreamStats;
import com.ss.bytertc.engine.type.SubscribeFallbackOptions;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.volcengine.vertcdemo.common.MLog;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SDKJoinChannelSuccessEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.rts.RTCRoomEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTCVideoEventHandlerWithRTS;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.voice.event.SDKAudioPropertiesEvent;
import com.volcengine.vertcdemo.voice.event.SDKStreamStatsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VoiceRTCManager {
    private static final String TAG = "VoiceRtcManager";

    public static final int VOLUME_INTERVAL_MS = 1000;
    public static final int VOLUME_SMOOTH = 8;
    private static RTCVideo mRTCVideo;
    private static RTCRoom mRTCRoom;
    private static VoiceRTSClient mRTSClient;

    private static final RTCVideoEventHandlerWithRTS mRTCVideoEventHandler = new RTCVideoEventHandlerWithRTS() {

        private SDKAudioPropertiesEvent.SDKAudioProperties mLocalProperties = null; // 本地音量记录

        /**
         * 本地音频包括使用 RTC SDK 内部机制采集的麦克风音频和屏幕音频。
         * @param audioPropertiesInfos 本地音频信息，详见 LocalAudioPropertiesInfo 。
         */
        @Override
        public void onLocalAudioPropertiesReport(LocalAudioPropertiesInfo[] audioPropertiesInfos) {
            super.onLocalAudioPropertiesReport(audioPropertiesInfos);
            if (audioPropertiesInfos == null) {
                return;
            }
            for (LocalAudioPropertiesInfo info : audioPropertiesInfos) {
                if (info.streamIndex == StreamIndex.STREAM_INDEX_MAIN) {
                    SDKAudioPropertiesEvent.SDKAudioProperties properties = new SDKAudioPropertiesEvent.SDKAudioProperties(
                            SolutionDataManager.ins().getUserId(),
                            info.audioPropertiesInfo);
                    mLocalProperties = properties;
                    List<SDKAudioPropertiesEvent.SDKAudioProperties> audioPropertiesList = new ArrayList<>();
                    audioPropertiesList.add(properties);
                    SolutionDemoEventManager.post(new SDKAudioPropertiesEvent(audioPropertiesList));
                    return;
                }
            }
        }

        /**
         * 远端用户的音频包括使用 RTC SDK 内部机制/自定义机制采集的麦克风音频和屏幕音频。
         * @param audioPropertiesInfos 远端音频信息，其中包含音频流属性、房间 ID、用户 ID ，详见 RemoteAudioPropertiesInfo。
         * @param totalRemoteVolume 订阅的所有远端流的总音量。
         */
        @Override
        public void onRemoteAudioPropertiesReport(RemoteAudioPropertiesInfo[] audioPropertiesInfos, int totalRemoteVolume) {
            super.onRemoteAudioPropertiesReport(audioPropertiesInfos, totalRemoteVolume);
            if (audioPropertiesInfos == null) {
                return;
            }
            List<SDKAudioPropertiesEvent.SDKAudioProperties> audioPropertiesList = new ArrayList<>();
            if (mLocalProperties != null) {
                audioPropertiesList.add(mLocalProperties);
            }
            for (RemoteAudioPropertiesInfo info : audioPropertiesInfos) {
                if (info.streamKey.getStreamIndex() == StreamIndex.STREAM_INDEX_MAIN) {
                    audioPropertiesList.add(new SDKAudioPropertiesEvent.SDKAudioProperties(
                            info.streamKey.getUserId(),
                            info.audioPropertiesInfo));
                }
            }
            SolutionDemoEventManager.post(new SDKAudioPropertiesEvent(audioPropertiesList));
        }

        @Override
        public void onError(int err) {
            Log.e(TAG, "onError:" + err);
        }
    };

    private static final RTCRoomEventHandlerWithRTS mRTCRoomEventHandler = new RTCRoomEventHandlerWithRTS() {

        private LocalStreamStats mLocalStreamStats; // 缓存上次本地音频统计数据
        private RemoteStreamStats mRemoteStreamStats;  // 缓存上次远端音频统计数据

        @Override
        public void onRoomStateChanged(String roomId, String uid, int state, String extraInfo) {
            super.onRoomStateChanged(roomId, uid, state, extraInfo);
            Log.d(TAG, String.format("onRoomStateChanged: %s, %s, %d, %s", roomId, uid, state, extraInfo));
            if (isFirstJoinRoomSuccess(state, extraInfo)) {
                SolutionDemoEventManager.post(new SDKJoinChannelSuccessEvent(roomId, uid));
            }
        }

        /**
         * 数据统计
         * 发布流成功后，每隔 2s 收到此回调，了解发布的流在此周期内的网络质量信息。
         * @param stats 音视频流以及网络状况统计信息。参见 LocalStreamStats。
         */
        @Override
        public void onLocalStreamStats(LocalStreamStats stats) {
            super.onLocalStreamStats(stats);
            mLocalStreamStats = stats;
            updateStreamStats();
        }

        /**
         * 数据统计
         * 每隔 2s 收到此回调，了解订阅的远端用户发布的流在此周期内的网络质量信息。
         * @param stats 音视频流以及网络状况统计信息。参见 RemoteStreamStats。
         */
        @Override
        public void onRemoteStreamStats(RemoteStreamStats stats) {
            super.onRemoteStreamStats(stats);
            mRemoteStreamStats = stats;
            updateStreamStats();
        }

        /**
         * 发送本地音频统计更新事件
         */
        private void updateStreamStats() {
            SDKStreamStatsEvent event = new SDKStreamStatsEvent();
            if (mLocalStreamStats != null) {
                event.audioChannel = mLocalStreamStats.audioStats.numChannels;
                event.delay = mLocalStreamStats.audioStats.rtt;
                event.uploadSampleRate = mLocalStreamStats.audioStats.sentSampleRate;
                event.uploadBitrate = mLocalStreamStats.audioStats.sendKBitrate;
                event.uploadLossRate = mLocalStreamStats.audioStats.audioLossRate;
            }
            if (mRemoteStreamStats != null) {
                event.downloadBitrate = mRemoteStreamStats.audioStats.receivedKBitrate;
                event.downloadLossRate = mRemoteStreamStats.audioStats.audioLossRate;
            }
            SolutionDemoEventManager.post(event);
        }
    };

    public static void createEngine(RTSInfo info) {
        if (mRTCVideo == null) {
            mRTCVideo = RTCVideo.createRTCVideo(Utilities.getApplicationContext(), info.appId,
                    mRTCVideoEventHandler, null, null);
            mRTCVideo.setBusinessId(info.bid);
            mRTSClient = new VoiceRTSClient(mRTCVideo, info);
            mRTCVideoEventHandler.setBaseClient(mRTSClient);
            enableLocalAudio(true);
            setRemoteSubscribeFallbackOption(SUBSCRIBE_FALLBACK_OPTIONS_AUDIO_ONLY);
            enableAudioVolumeIndication(VOLUME_INTERVAL_MS, VOLUME_SMOOTH);
        }
    }

    public static VoiceRTSClient getRTSClient() {
        return mRTSClient;
    }

    public static void destroyEngine() {
        MLog.d(TAG, "destroyEngine");
        if (mRTCRoom != null) {
            mRTCRoom.destroy();
        }
        if (mRTCVideo == null) {
            return;
        }
        RTCVideo.destroyRTCVideo();
        mRTCVideo = null;
    }

    public static void muteLocalAudioStream(boolean mute) {
        MLog.d(TAG, "muteLocalAudioStream");
        if (mRTCRoom == null) {
            return;
        }
        if (mute) {
            mRTCRoom.unpublishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        } else {
            mRTCRoom.publishStream(MediaStreamType.RTC_MEDIA_STREAM_TYPE_AUDIO);
        }
    }

    public static void enableLocalAudio(boolean enable) {
        MLog.d(TAG, "enableLocalAudio: " + enable);
        if (mRTCVideo == null) {
            return;
        }
        if (enable) {
            mRTCVideo.startAudioCapture();
        } else {
            mRTCVideo.stopAudioCapture();
        }
    }

    public static void joinRoom(String token, String roomId, String uid) {
        MLog.d(TAG, String.format(Locale.ENGLISH, "joinRoom: %s  %s  %s", token, roomId, uid));
        leaveRoom();
        if (mRTCVideo == null) {
            return;
        }
        mRTCRoom = mRTCVideo.createRTCRoom(roomId);
        mRTCRoom.setRTCRoomEventHandler(mRTCRoomEventHandler);
        mRTCRoomEventHandler.setBaseClient(mRTSClient);
        UserInfo userInfo = new UserInfo(uid, null);
        RTCRoomConfig roomConfig = new RTCRoomConfig(ChannelProfile.CHANNEL_PROFILE_COMMUNICATION,
                true, true, false);
        mRTCRoom.joinRoom(token, userInfo, roomConfig);
    }

    public static void leaveRoom() {
        MLog.d(TAG, "leaveRoom");
        if (mRTCRoom != null) {
            mRTCRoom.leaveRoom();
            mRTCRoom.destroy();
        }
    }

    public static void enableAudioVolumeIndication(int interval, int smooth) {
        MLog.d(TAG, String.format(Locale.ENGLISH, "enableAudioVolumeIndication : %d  %d", interval, smooth));
        if (mRTCVideo == null) {
            return;
        }
        AudioPropertiesConfig config = new AudioPropertiesConfig(interval);
        mRTCVideo.enableAudioPropertiesReport(config);
    }

    public static void setRemoteSubscribeFallbackOption(SubscribeFallbackOptions options) {
        MLog.d(TAG, "setRemoteSubscribeFallbackOption: " + options.toString());
        if (mRTCVideo == null) {
            return;
        }
        mRTCVideo.setSubscribeFallbackOption(options);
    }
}
