#import "VoiceRTCManager.h"
#import "AlertActionManager.h"

@interface VoiceRTCManager () <ByteRTCEngineDelegate>

@property (nonatomic, strong) VoiceRoomParamInfoModel *paramInfoModel;

@end

@implementation VoiceRTCManager

+ (VoiceRTCManager *_Nullable)shareRtc {
    static VoiceRTCManager *voiceRTCManager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        voiceRTCManager = [[VoiceRTCManager alloc] init];
    });
    return voiceRTCManager;
}

#pragma mark - Publish Action

- (void)joinChannelWithToken:(NSString *)token roomID:(NSString *)roomID uid:(NSString *)uid {
    //设置订阅的音视频流回退选项
    //Set the subscribed audio and video stream fallback options
    [self.rtcEngineKit setSubscribeFallbackOption:ByteRTCSubscribeFallbackOptionAudioOnly];

    //关闭 本地音频/视频采集
    //Turn on/off local audio capture
    [self.rtcEngineKit stopAudioCapture];
    [self.rtcEngineKit stopVideoCapture];

    //设置音频路由模式，YES 扬声器/NO 听筒
    //Set the audio routing mode, YES speaker/NO earpiece
    [self.rtcEngineKit setAudioPlaybackDevice:ByteRTCAudioPlaybackDeviceSpeakerphone];
    
    //开启/关闭发言者音量键控
    //Turn on/off speaker volume keying
    [self.rtcEngineKit setAudioVolumeIndicationInterval:200];

    //加入房间，开始连麦,需要申请AppId和Token
    //Join the room, start connecting the microphone, you need to apply for AppId and Token
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = uid;
    
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileLiveBroadcasting;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    
    [self.rtcEngineKit joinRoomByKey:token
                              roomId:roomID
                            userInfo:userInfo
                       rtcRoomConfig:config];
    
    //设置用户为隐身状态
    //Set user to incognito state
    [self.rtcEngineKit setUserVisibility:NO];
}

#pragma mark - rtc method

- (void)makeCoHost:(BOOL)isCoHost {
    //开启/关闭 本地音频采集
    //Turn on/off local audio capture
    if (isCoHost) {
        [self.rtcEngineKit setUserVisibility:YES];
        [self.rtcEngineKit startAudioCapture];
        [self.rtcEngineKit publishStream:ByteRTCMediaStreamTypeAudio];
    } else {
        [self.rtcEngineKit setUserVisibility:NO];
        [self.rtcEngineKit stopAudioCapture];
    }
}

- (void)muteLocalAudioStream:(BOOL)isMute {
    //开启/关闭 本地音频推流
    //Turn on/off local audio stream
    if (isMute) {
        [self.rtcEngineKit unpublishStream:ByteRTCMediaStreamTypeAudio];
    } else {
        [self.rtcEngineKit publishStream:ByteRTCMediaStreamTypeAudio];
    }
}

- (void)leaveRTCRoom {
    //离开频道
    //Leave the channel
    [self makeCoHost:NO];
    [self muteLocalAudioStream:YES];
    [self.rtcEngineKit leaveRoom];
}

#pragma mark - ByteRTCEngineDelegate

- (void)rtcEngine:(ByteRTCEngineKit *_Nonnull)engine onLocalStreamStats:(const ByteRTCLocalStreamStats * _Nonnull)stats {
    self.paramInfoModel.numChannels = [NSString stringWithFormat:@"%.0ld",(long)stats.audio_stats.numChannels];
    self.paramInfoModel.sentSampleRate = [NSString stringWithFormat:@"%.0ld",(long)stats.audio_stats.sentSampleRate];
    self.paramInfoModel.sentKBitrate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.sentKBitrate];
    self.paramInfoModel.audioLossRate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.audioLossRate];
    
    self.paramInfoModel.rtt = [NSString stringWithFormat:@"%.0ld",(long)stats.audio_stats.rtt];
    
    [self updateRoomParamInfoModel];
}

- (void)rtcEngine:(ByteRTCEngineKit * _Nonnull)engine onRemoteStreamStats:(const ByteRTCRemoteStreamStats * _Nonnull)stats {
    self.paramInfoModel.recordKBitrate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.receivedKBitrate];;
    self.paramInfoModel.recordLossRate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.audioLossRate];;
    
    [self updateRoomParamInfoModel];
}

- (void)rtcEngine:(ByteRTCEngineKit * _Nonnull)engine onAudioVolumeIndication:(NSArray<ByteRTCAudioVolumeInfo *> * _Nonnull)speakers totalRemoteVolume:(NSInteger)totalRemoteVolume {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    for (int i = 0; i < speakers.count; i++) {
        ByteRTCAudioVolumeInfo *model = speakers[i];
        [dic setValue:@(model.linearVolume) forKey:model.uid];
    }
    if ([self.delegate respondsToSelector:@selector(voiceRTCManager:reportAllAudioVolume:)]) {
        [self.delegate voiceRTCManager:self reportAllAudioVolume:dic];
    }
}

#pragma mark - Private Action

- (void)updateRoomParamInfoModel {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(voiceRTCManager:changeParamInfo:)]) {
            [self.delegate voiceRTCManager:self changeParamInfo:self.paramInfoModel];
        }
    });
}

#pragma mark - getter

- (VoiceRoomParamInfoModel *)paramInfoModel {
    if (!_paramInfoModel) {
        _paramInfoModel = [[VoiceRoomParamInfoModel alloc] init];
    }
    return _paramInfoModel;
}

@end
