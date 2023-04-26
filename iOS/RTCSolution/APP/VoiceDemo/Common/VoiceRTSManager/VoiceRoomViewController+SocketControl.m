// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VoiceRoomViewController+SocketControl.h"

@implementation VoiceRoomViewController (SocketControl)

- (void)addSocketListener {
    __weak __typeof(self) wself = self;
    [VoiceRTSManager onJoinMeetingWithBlock:^(VoiceControlUserModel * _Nonnull userModel) {
        if (wself) {
            [wself addUser:userModel];
        }
    }];
    
    [VoiceRTSManager onLeaveMeetingWithBlock:^(VoiceControlUserModel * _Nonnull userModel) {
        if (wself) {
            [wself removeUser:userModel];
        }
    }];

    [VoiceRTSManager onRaiseHandsMicWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            //Received notification of raising hands
            [wself receivedRaiseHandWithUser:uid];
        }
    }];
    
    [VoiceRTSManager onInviteMicWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            //The audience receives the invitation to the microphone notification
            [wself receivedRaiseHandInviteWithAudience];
        }
    }];
    
    [VoiceRTSManager onMicOnWithBlock:^(VoiceControlUserModel * _Nonnull userModel) {
        if (wself) {
            //Notification of successful user registration
            [wself receivedRaiseHandSucceedWithUser:userModel];
        }
    }];
    
    [VoiceRTSManager onMicOffWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            //Notification of successful user downloading
            [wself receivedLowerHandSucceedWithUser:uid];
        }
    }];
    
    [VoiceRTSManager onMuteMicWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            [wself receivedMicChangeWithMute:YES uid:uid];
        }
    }];
    
    [VoiceRTSManager onUnmuteMic:^(NSString * _Nonnull uid) {
        if (wself) {
            [wself receivedMicChangeWithMute:NO uid:uid];
        }
    }];
    
    [VoiceRTSManager onHostChange:^(NSString * _Nonnull formerHostID, VoiceControlUserModel *hostUser) {
        if (wself) {
            [wself receivedHostChangeWithNewHostUid:hostUser];
        }
    }];

    [VoiceRTSManager onMeetingEnd:^(BOOL result) {
        if (wself) {
            [wself receivedMeetingEnd];
        }
    }];
}
@end
