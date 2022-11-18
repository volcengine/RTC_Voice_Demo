//
//  VoiceRoomViewController+SocketControl.m
//  veRTC_Demo
//
//  Created by on 2021/5/28.
//  
//

#import "VoiceRoomViewController+SocketControl.h"

@implementation VoiceRoomViewController (SocketControl)

- (void)addSocketListener {
    __weak __typeof(self) wself = self;
    [VoiceRTMManager onJoinMeetingWithBlock:^(VoiceControlUserModel * _Nonnull userModel) {
        if (wself) {
            [wself addUser:userModel];
        }
    }];
    
    [VoiceRTMManager onLeaveMeetingWithBlock:^(VoiceControlUserModel * _Nonnull userModel) {
        if (wself) {
            [wself removeUser:userModel];
        }
    }];

    [VoiceRTMManager onRaiseHandsMicWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            //Received notification of raising hands
            [wself receivedRaiseHandWithUser:uid];
        }
    }];
    
    [VoiceRTMManager onInviteMicWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            //The audience receives the invitation to the microphone notification
            [wself receivedRaiseHandInviteWithAudience];
        }
    }];
    
    [VoiceRTMManager onMicOnWithBlock:^(VoiceControlUserModel * _Nonnull userModel) {
        if (wself) {
            //Notification of successful user registration
            [wself receivedRaiseHandSucceedWithUser:userModel];
        }
    }];
    
    [VoiceRTMManager onMicOffWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            //Notification of successful user downloading
            [wself receivedLowerHandSucceedWithUser:uid];
        }
    }];
    
    [VoiceRTMManager onMuteMicWithBlock:^(NSString * _Nonnull uid) {
        if (wself) {
            [wself receivedMicChangeWithMute:YES uid:uid];
        }
    }];
    
    [VoiceRTMManager onUnmuteMic:^(NSString * _Nonnull uid) {
        if (wself) {
            [wself receivedMicChangeWithMute:NO uid:uid];
        }
    }];
    
    [VoiceRTMManager onHostChange:^(NSString * _Nonnull formerHostID, VoiceControlUserModel *hostUser) {
        if (wself) {
            [wself receivedHostChangeWithNewHostUid:hostUser];
        }
    }];

    [VoiceRTMManager onMeetingEnd:^(BOOL result) {
        if (wself) {
            [wself receivedMeetingEnd];
        }
    }];
}
@end
