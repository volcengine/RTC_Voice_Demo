// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "BaseRTCManager.h"
#import "VoiceRoomParamInfoModel.h"

NS_ASSUME_NONNULL_BEGIN
@class VoiceRTCManager;
@protocol VoiceRTCManagerDelegate <NSObject>

/**
 * @brief 房间状态改变时的回调。 通过此回调，您会收到与房间相关的警告、错误和事件的通知。 例如，用户加入房间，用户被移出房间等。
 * @param manager GameRTCManager 模型
 * @param joinModel RTCJoinModel模型房间信息、加入成功失败等信息。
 */
- (void)voiceRTCManager:(VoiceRTCManager *)manager
     onRoomStateChanged:(RTCJoinModel *)joinModel;


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
 * Join RTC room
 * @param token token
 * @param roomID roomID
 * @param uid uid
 */
- (void)joinRTCRoomWithToken:(NSString *)token
                      roomID:(NSString *)roomID
                         uid:(NSString *)uid;

/*
 * CoHost role switching
 * @param enable ture:Turn on mask CoHost false：Turn off Lost CoHost
 */
- (void)makeCoHost:(BOOL)isCoHost;

/*
 * Switch local audio publish
 * @param mute ture:Turn on audio publish false：Turn off audio publish
 */
- (void)muteLocalAudioStream:(BOOL)isMute;

/*
 * Leave the room
 */
- (void)leaveRTCRoom;

@end

NS_ASSUME_NONNULL_END
