#import "VoiceRTCManager.h"

@interface VoiceRTCManager () <ByteRTCVideoDelegate>

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

- (void)joinRTCRoomWithToken:(NSString *)token
                      roomID:(NSString *)roomID
                         uid:(NSString *)uid {
    //关闭 本地音频/视频采集
    //Turn on/off local audio capture
    [self.rtcEngineKit stopAudioCapture];
    [self.rtcEngineKit stopVideoCapture];

    //设置音频路由模式，YES 扬声器/NO 听筒
    //Set the audio routing mode, YES speaker/NO earpiece
    [self.rtcEngineKit setDefaultAudioRoute:ByteRTCAudioRouteSpeakerphone];

    //开启/关闭发言者音量监控
    //Turn on/off speaker volume keying
    ByteRTCAudioPropertiesConfig *audioPropertiesConfig = [[ByteRTCAudioPropertiesConfig alloc] init];
    audioPropertiesConfig.interval = 200;
    [self.rtcEngineKit enableAudioPropertiesReport:audioPropertiesConfig];
    
    //加入 RTS 和 RTC 房间，开始连麦，需要申请AppId和Token
    //Join RTS and RTC room, start connecting the microphone, you need to apply for AppId and Token
    ByteRTCUserInfo *userInfo = [[ByteRTCUserInfo alloc] init];
    userInfo.userId = uid;
    ByteRTCRoomConfig *config = [[ByteRTCRoomConfig alloc] init];
    config.profile = ByteRTCRoomProfileInteractivePodcast;
    config.isAutoPublish = YES;
    config.isAutoSubscribeAudio = YES;
    self.rtcRoom = [self.rtcEngineKit createRTCRoom:roomID];
    self.rtcRoom.delegate = self;
    [self.rtcRoom joinRoomByToken:token userInfo:userInfo roomConfig:config];
    
    //设置用户默认为隐身状态
    //Set user to incognito state
    [self.rtcRoom setUserVisibility:NO];
}

#pragma mark - rtc method

- (void)makeCoHost:(BOOL)isCoHost {
    //上麦/下麦 操作
    //Turn on/off local audio capture
    if (isCoHost) {
        [self.rtcRoom setUserVisibility:YES];
        [self.rtcEngineKit startAudioCapture];
        [self.rtcRoom publishStream:ByteRTCMediaStreamTypeAudio];
    } else {
        [self.rtcRoom setUserVisibility:NO];
        [self.rtcEngineKit stopAudioCapture];
    }
}

- (void)muteLocalAudioStream:(BOOL)isMute {
    //开启/关闭 本地音频推流
    //Turn on/off local audio stream
    if (isMute) {
        [self.rtcRoom unpublishStream:ByteRTCMediaStreamTypeAudio];
    } else {
        [self.rtcRoom publishStream:ByteRTCMediaStreamTypeAudio];
    }
}

- (void)leaveRTCRoom {
    //离开频道
    //Leave the channel
    [self makeCoHost:NO];
    [self muteLocalAudioStream:YES];
    [self.rtcRoom leaveRoom];
}

#pragma mark - ByteRTCVideoDelegate

- (void)rtcEngine:(ByteRTCVideo *)engine onRemoteAudioPropertiesReport:(NSArray<ByteRTCRemoteAudioPropertiesInfo *> *)audioPropertiesInfos totalRemoteVolume:(NSInteger)totalRemoteVolume {
    NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
    for (int i = 0; i < audioPropertiesInfos.count; i++) {
        ByteRTCRemoteAudioPropertiesInfo *model = audioPropertiesInfos[i];
        [dic setValue:@(model.audioPropertiesInfo.linearVolume) forKey:model.streamKey.userId];
    }
    if ([self.delegate respondsToSelector:@selector(voiceRTCManager:reportAllAudioVolume:)]) {
        [self.delegate voiceRTCManager:self reportAllAudioVolume:dic];
    }
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onLocalStreamStats:(ByteRTCLocalStreamStats *)stats {
    self.paramInfoModel.numChannels = [NSString stringWithFormat:@"%.0ld",(long)stats.audio_stats.numChannels];
    self.paramInfoModel.sentSampleRate = [NSString stringWithFormat:@"%.0ld",(long)stats.audio_stats.sentSampleRate];
    self.paramInfoModel.sentKBitrate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.sentKBitrate];
    self.paramInfoModel.audioLossRate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.audioLossRate];
    
    self.paramInfoModel.rtt = [NSString stringWithFormat:@"%.0ld",(long)stats.audio_stats.rtt];
    
    [self updateRoomParamInfoModel];
}

- (void)rtcRoom:(ByteRTCRoom *)rtcRoom onRemoteStreamStats:(ByteRTCRemoteStreamStats *)stats {
    self.paramInfoModel.recordKBitrate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.receivedKBitrate];;
    self.paramInfoModel.recordLossRate = [NSString stringWithFormat:@"%.0f",stats.audio_stats.audioLossRate];;
    
    [self updateRoomParamInfoModel];
}

#pragma mark - Private Action

- (void)updateRoomParamInfoModel {
    dispatch_async(dispatch_get_main_queue(), ^{
        if ([self.delegate respondsToSelector:@selector(voiceRTCManager:changeParamInfo:)]) {
            [self.delegate voiceRTCManager:self changeParamInfo:self.paramInfoModel];
        }
    });
}

#pragma mark - Getter

- (VoiceRoomParamInfoModel *)paramInfoModel {
    if (!_paramInfoModel) {
        _paramInfoModel = [[VoiceRoomParamInfoModel alloc] init];
    }
    return _paramInfoModel;
}

@end
