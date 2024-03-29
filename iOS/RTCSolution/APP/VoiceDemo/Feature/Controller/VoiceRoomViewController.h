// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceRTSManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomViewController : UIViewController

- (instancetype)initWithToken:(NSString *)token
                    roomModel:(VoiceControlRoomModel *)roomModel
                    userLists:(NSArray<VoiceControlUserModel *> *)userLists;

- (void)addUser:(VoiceControlUserModel *)userModel;

- (void)removeUser:(VoiceControlUserModel *)userModel;

- (void)receivedRaiseHandWithUser:(NSString *)uid;

- (void)receivedRaiseHandInviteWithAudience;

- (void)receivedRaiseHandSucceedWithUser:(VoiceControlUserModel *)userModel;

- (void)receivedLowerHandSucceedWithUser:(NSString *)uid;

- (void)receivedMicChangeWithMute:(BOOL)isMute uid:(NSString *)uid;

- (void)receivedHostChangeWithNewHostUid:(VoiceControlUserModel *)hostUser;

- (void)receivedMeetingEnd;

@end

NS_ASSUME_NONNULL_END
