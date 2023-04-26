// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

//UI
typedef NS_ENUM(NSInteger, VoiceButtonColorType) {
    VoiceButtonColorTypeNone,
    VoiceButtonColorTypeRemind,
};

//form
typedef NS_ENUM(NSInteger, VoiceEndStatus) {
    VoiceEndStatusAudience,
    VoiceEndStatusHost,
    VoiceEndStatusHostOnly,
};

//button status
typedef NS_ENUM(NSInteger, VoiceButtonStatus) {
    VoiceButtonStatusEnd,
    VoiceButtonStatusLeave,
    VoiceButtonStatusCancel,
};

@interface VoiceRoomEndView : UIView

@property (nonatomic, copy) void (^clickButtonBlock)(VoiceButtonStatus status);

@property (nonatomic, assign) VoiceEndStatus VoiceEndStatus;

@end

NS_ASSUME_NONNULL_END
