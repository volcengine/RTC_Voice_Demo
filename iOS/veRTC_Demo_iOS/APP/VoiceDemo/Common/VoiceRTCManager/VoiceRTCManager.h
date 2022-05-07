#import "BaseRTCManager.h"
#import "VoiceRTCManager.h"
#import <VolcEngineRTC/objc/rtc/ByteRTCEngineKit.h>
#import "VoiceRoomParamInfoModel.h"

NS_ASSUME_NONNULL_BEGIN
@class VoiceRTCManager;
@protocol VoiceRTCManagerDelegate <NSObject>

- (void)voiceRTCManager:(VoiceRTCManager *)voiceRTCManager changeParamInfo:(VoiceRoomParamInfoModel *)model;

- (void)voiceRTCManager:(VoiceRTCManager *_Nonnull)voiceRTCManager reportAllAudioVolume:(NSDictionary<NSString *, NSNumber *> *_Nonnull)volumeInfo;

@end

@interface VoiceRTCManager : BaseRTCManager

@property (nonatomic, weak) id<VoiceRTCManagerDelegate> delegate;

/*
 * RTC Manager Singletons
 */
+ (VoiceRTCManager *_Nullable)shareRtc;

#pragma mark - Base Method

/**
 * Join room
 * @param token token
 * @param roomID roomID
 * @param uid uid
 */
- (void)joinChannelWithToken:(NSString *)token roomID:(NSString *)roomID uid:(NSString *)uid;

/*
 * Switch local audio capture
 * @param enable ture:Turn on audio capture false：Turn off audio capture
 */
- (void)makeCoHost:(BOOL)isCoHost;

/*
 * Switch local audio capture
 * @param mute ture:Turn on audio capture false：Turn off audio capture
 */
- (void)muteLocalAudioStream:(BOOL)isMute;

/*
 * Leave the room
 */
- (void)leaveRTCRoom;

@end

NS_ASSUME_NONNULL_END
