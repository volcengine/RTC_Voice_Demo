// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceRTSManager.h"
@class VoiceRoomNavView;

typedef NS_ENUM(NSInteger, RoomNavStatus) {
    RoomNavStatusHangeup
};

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomNavViewDelegate <NSObject>

- (void)voiceRoomNavView:(VoiceRoomNavView *)voiceRoomNavView didSelectStatus:(RoomNavStatus)status;

@end

@interface VoiceRoomNavView : UIView

@property (nonatomic, strong) VoiceControlRoomModel *roomModel;

@property (nonatomic, weak) id<VoiceRoomNavViewDelegate> delegate;

@property (nonatomic, assign) NSInteger meetingTime;

@end

NS_ASSUME_NONNULL_END
